package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.RiskAppeal;
import com.scaffolding.entity.RiskRecord;
import com.scaffolding.entity.User;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.RiskAppealMapper;
import com.scaffolding.mapper.RiskRecordMapper;
import com.scaffolding.mapper.UserMapper;
import com.scaffolding.service.RiskAppealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险申诉服务实现类
 * 
 * @author scaffolding
 */
@Service
public class RiskAppealServiceImpl extends ServiceImpl<RiskAppealMapper, RiskAppeal> implements RiskAppealService {

    @Autowired
    private RiskRecordMapper riskRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<RiskAppeal> pageQuery(Long current, Long size, Long userId, Long riskRecordId, String status) {
        Page<RiskAppeal> page = new Page<>(current, size);
        LambdaQueryWrapper<RiskAppeal> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(RiskAppeal::getUserId, userId);
        }
        if (riskRecordId != null) {
            wrapper.eq(RiskAppeal::getRiskRecordId, riskRecordId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(RiskAppeal::getStatus, status);
        }
        
        wrapper.orderByDesc(RiskAppeal::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<RiskAppeal> getAppealsByUserId(Long userId) {
        LambdaQueryWrapper<RiskAppeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskAppeal::getUserId, userId);
        wrapper.orderByDesc(RiskAppeal::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public RiskAppeal submitAppeal(RiskAppeal appeal) {
        if (!canSubmitAppeal(appeal.getRiskRecordId(), appeal.getUserId())) {
            throw new BusinessException("当前无法提交申诉，请等待下次可申诉时间");
        }

        RiskRecord riskRecord = riskRecordMapper.selectById(appeal.getRiskRecordId());
        if (riskRecord == null) {
            throw new BusinessException("风险记录不存在");
        }

        appeal.setCreateTime(LocalDateTime.now());
        appeal.setUpdateTime(LocalDateTime.now());
        appeal.setStatus("pending");
        this.save(appeal);

        riskRecord.setStatus("appealing");
        riskRecord.setUpdateTime(LocalDateTime.now());
        riskRecordMapper.updateById(riskRecord);

        return appeal;
    }

    @Override
    public RiskAppeal enterpriseReview(Long appealId, Long reviewerId, String reviewerName, String comment, boolean approved) {
        RiskAppeal appeal = this.getById(appealId);
        if (appeal == null) {
            throw new BusinessException("申诉记录不存在");
        }
        if (!"pending".equals(appeal.getStatus())) {
            throw new BusinessException("当前状态不支持企业复核");
        }

        appeal.setEnterpriseReviewUserId(reviewerId);
        appeal.setEnterpriseReviewUserName(reviewerName);
        appeal.setEnterpriseReviewComment(comment);
        appeal.setEnterpriseReviewTime(LocalDateTime.now());

        if (!approved) {
            appeal.setStatus("rejected");
            appeal.setUpdateTime(LocalDateTime.now());
            this.updateById(appeal);

            RiskRecord riskRecord = riskRecordMapper.selectById(appeal.getRiskRecordId());
            if (riskRecord != null) {
                riskRecord.setStatus("rejected");
                if (appeal.getNextAppealTime() != null) {
                    riskRecord.setNextAppealTime(appeal.getNextAppealTime());
                }
                riskRecord.setUpdateTime(LocalDateTime.now());
                riskRecordMapper.updateById(riskRecord);
            }
        } else {
            appeal.setStatus("reviewing");
            appeal.setUpdateTime(LocalDateTime.now());
            this.updateById(appeal);
        }

        return appeal;
    }

    @Override
    public RiskAppeal platformReview(Long appealId, Long reviewerId, String reviewerName, String comment, boolean approved, LocalDateTime nextAppealTime) {
        RiskAppeal appeal = this.getById(appealId);
        if (appeal == null) {
            throw new BusinessException("申诉记录不存在");
        }

        appeal.setPlatformReviewUserId(reviewerId);
        appeal.setPlatformReviewUserName(reviewerName);
        appeal.setPlatformReviewComment(comment);
        appeal.setPlatformReviewTime(LocalDateTime.now());
        appeal.setFinalComment(comment);

        RiskRecord riskRecord = riskRecordMapper.selectById(appeal.getRiskRecordId());

        if (approved) {
            appeal.setStatus("approved");
            appeal.setUpdateTime(LocalDateTime.now());
            this.updateById(appeal);

            if (riskRecord != null) {
                riskRecord.setStatus("resolved");
                riskRecord.setUpdateTime(LocalDateTime.now());
                riskRecordMapper.updateById(riskRecord);

                User user = userMapper.selectById(riskRecord.getUserId());
                if (user != null) {
                    user.setRiskLevel("normal");
                    user.setRestrictReason(null);
                    user.setRestrictTime(null);
                    userMapper.updateById(user);
                }
            }
        } else {
            appeal.setStatus("rejected");
            appeal.setNextAppealTime(nextAppealTime);
            appeal.setUpdateTime(LocalDateTime.now());
            this.updateById(appeal);

            if (riskRecord != null) {
                riskRecord.setStatus("rejected");
                riskRecord.setNextAppealTime(nextAppealTime);
                riskRecord.setUpdateTime(LocalDateTime.now());
                riskRecordMapper.updateById(riskRecord);
            }
        }

        return appeal;
    }

    @Override
    public boolean canSubmitAppeal(Long riskRecordId, Long userId) {
        RiskRecord riskRecord = riskRecordMapper.selectById(riskRecordId);
        if (riskRecord == null) {
            return false;
        }

        if (!riskRecord.getUserId().equals(userId)) {
            return false;
        }

        if ("resolved".equals(riskRecord.getStatus())) {
            return false;
        }

        if (riskRecord.getNextAppealTime() != null && riskRecord.getNextAppealTime().isAfter(LocalDateTime.now())) {
            return false;
        }

        LambdaQueryWrapper<RiskAppeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskAppeal::getRiskRecordId, riskRecordId);
        wrapper.eq(RiskAppeal::getUserId, userId);
        wrapper.in(RiskAppeal::getStatus, "pending", "reviewing");
        Long count = this.count(wrapper);
        return count == 0;
    }

    @Override
    public LocalDateTime getNextAppealTime(Long riskRecordId, Long userId) {
        RiskRecord riskRecord = riskRecordMapper.selectById(riskRecordId);
        if (riskRecord != null && riskRecord.getNextAppealTime() != null) {
            return riskRecord.getNextAppealTime();
        }

        LambdaQueryWrapper<RiskAppeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskAppeal::getRiskRecordId, riskRecordId);
        wrapper.eq(RiskAppeal::getUserId, userId);
        wrapper.eq(RiskAppeal::getStatus, "rejected");
        wrapper.orderByDesc(RiskAppeal::getCreateTime);
        wrapper.last("LIMIT 1");
        RiskAppeal lastRejected = this.getOne(wrapper, false);
        if (lastRejected != null && lastRejected.getNextAppealTime() != null) {
            return lastRejected.getNextAppealTime();
        }

        return null;
    }
}

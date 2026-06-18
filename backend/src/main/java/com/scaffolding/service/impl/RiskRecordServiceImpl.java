package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.RiskRecord;
import com.scaffolding.entity.User;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.RiskRecordMapper;
import com.scaffolding.mapper.UserMapper;
import com.scaffolding.service.RiskRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险记录服务实现类
 * 
 * @author scaffolding
 */
@Service
public class RiskRecordServiceImpl extends ServiceImpl<RiskRecordMapper, RiskRecord> implements RiskRecordService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<RiskRecord> pageQuery(Long current, Long size, Long userId, String riskType, String status, String riskLevel) {
        Page<RiskRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<RiskRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(RiskRecord::getUserId, userId);
        }
        if (StringUtils.hasText(riskType)) {
            wrapper.eq(RiskRecord::getRiskType, riskType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(RiskRecord::getStatus, status);
        }
        if (StringUtils.hasText(riskLevel)) {
            wrapper.eq(RiskRecord::getRiskLevel, riskLevel);
        }
        
        wrapper.orderByDesc(RiskRecord::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<RiskRecord> getActiveRiskRecords(Long userId) {
        LambdaQueryWrapper<RiskRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskRecord::getUserId, userId);
        wrapper.in(RiskRecord::getStatus, "active", "appealing");
        wrapper.orderByDesc(RiskRecord::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public boolean canApplyForHighRiskWork(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        if ("banned".equals(user.getRiskLevel())) {
            return false;
        }
        
        if ("restricted".equals(user.getRiskLevel())) {
            RiskRecord restriction = getApplyRestriction(userId);
            if (restriction != null) {
                if (restriction.getRestrictEndTime() != null 
                    && restriction.getRestrictEndTime().isBefore(LocalDateTime.now())) {
                    return true;
                }
                return false;
            }
        }
        
        return true;
    }

    @Override
    public RiskRecord getApplyRestriction(Long userId) {
        LambdaQueryWrapper<RiskRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskRecord::getUserId, userId);
        wrapper.in(RiskRecord::getStatus, "active", "appealing");
        wrapper.orderByDesc(RiskRecord::getCreateTime);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper, false);
    }

    @Override
    public RiskRecord addRiskRecord(RiskRecord riskRecord) {
        riskRecord.setCreateTime(LocalDateTime.now());
        riskRecord.setUpdateTime(LocalDateTime.now());
        if (!StringUtils.hasText(riskRecord.getStatus())) {
            riskRecord.setStatus("active");
        }
        this.save(riskRecord);

        User user = userMapper.selectById(riskRecord.getUserId());
        if (user != null) {
            user.setRiskLevel(riskRecord.getRiskLevel());
            user.setRestrictReason(riskRecord.getDescription());
            user.setRestrictTime(LocalDateTime.now());
            userMapper.updateById(user);
        }

        return riskRecord;
    }

    @Override
    public boolean resolveRiskRecord(Long id, Long operatorId, String operatorName) {
        RiskRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("风险记录不存在");
        }

        record.setStatus("resolved");
        record.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(record);

        if (result) {
            refreshUserRiskLevel(record.getUserId());
        }

        return result;
    }

    /**
     * 根据用户所有活跃风险记录，重新计算并更新用户的 riskLevel
     */
    private void refreshUserRiskLevel(Long userId) {
        LambdaQueryWrapper<RiskRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskRecord::getUserId, userId);
        wrapper.in(RiskRecord::getStatus, "active", "appealing");
        wrapper.orderByDesc(RiskRecord::getCreateTime);
        List<RiskRecord> activeRecords = this.list(wrapper);

        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        if (activeRecords == null || activeRecords.isEmpty()) {
            user.setRiskLevel("normal");
            user.setRestrictReason(null);
            user.setRestrictTime(null);
            userMapper.updateById(user);
            return;
        }

        boolean hasBanned = activeRecords.stream()
                .anyMatch(r -> "banned".equals(r.getRiskLevel()));
        RiskRecord latest = activeRecords.get(0);

        if (hasBanned) {
            user.setRiskLevel("banned");
        } else {
            user.setRiskLevel("restricted");
        }
        user.setRestrictReason(latest.getDescription());
        user.setRestrictTime(latest.getCreateTime());
        userMapper.updateById(user);
    }
}

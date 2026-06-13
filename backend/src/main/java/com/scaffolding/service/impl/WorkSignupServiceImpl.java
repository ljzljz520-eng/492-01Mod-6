package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.RiskRecord;
import com.scaffolding.entity.Work;
import com.scaffolding.entity.WorkSignup;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.WorkMapper;
import com.scaffolding.mapper.WorkSignupMapper;
import com.scaffolding.service.RiskRecordService;
import com.scaffolding.service.WorkSignupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作报名服务实现类
 * 
 * @author scaffolding
 */
@Service
public class WorkSignupServiceImpl extends ServiceImpl<WorkSignupMapper, WorkSignup> implements WorkSignupService {

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private RiskRecordService riskRecordService;

    @Override
    public Page<WorkSignup> pageQuery(Long current, Long size, Long workId, Long userId, String status) {
        Page<WorkSignup> page = new Page<>(current, size);
        LambdaQueryWrapper<WorkSignup> wrapper = new LambdaQueryWrapper<>();

        if (workId != null) {
            wrapper.eq(WorkSignup::getWorkId, workId);
        }
        if (userId != null) {
            wrapper.eq(WorkSignup::getUserId, userId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(WorkSignup::getStatus, status);
        }

        wrapper.orderByDesc(WorkSignup::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Map<String, Object> signupWork(Long workId, Long userId, String userName) {
        Map<String, Object> result = new HashMap<>();

        Work work = workMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("工作不存在");
        }

        LambdaQueryWrapper<WorkSignup> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(WorkSignup::getWorkId, workId);
        existWrapper.eq(WorkSignup::getUserId, userId);
        existWrapper.in(WorkSignup::getStatus, "pending", "approved");
        Long existCount = this.count(existWrapper);
        if (existCount > 0) {
            throw new BusinessException("您已报名该工作，不能重复报名");
        }

        boolean riskCheckPassed = true;
        String riskReason = null;

        if (work.getIsHighRisk() != null && work.getIsHighRisk() == 1) {
            riskCheckPassed = riskRecordService.canApplyForHighRiskWork(userId);
            if (!riskCheckPassed) {
                RiskRecord restriction = riskRecordService.getApplyRestriction(userId);
                if (restriction != null) {
                    riskReason = "您存在风险记录（" + getRiskTypeText(restriction.getRiskType())
                            + "），当前不能报名高风险岗位。状态：" + getRiskStatusText(restriction.getStatus())
                            + "。如有异议，请提交申诉。";
                } else {
                    riskReason = "您的账号当前被限制报名高风险岗位，请提交申诉或联系平台客服。";
                }
                result.put("riskCheckPassed", false);
                result.put("riskReason", riskReason);
                result.put("restriction", restriction);
                result.put("signupId", null);
                result.put("message", "报名失败：" + riskReason);
                return result;
            }
        }

        WorkSignup signup = new WorkSignup();
        signup.setWorkId(workId);
        signup.setWorkName(work.getWorkName());
        signup.setUserId(userId);
        signup.setUserName(userName);
        signup.setStatus("pending");
        signup.setRiskCheckPassed(riskCheckPassed ? 1 : 0);
        signup.setRiskReason(riskReason);
        signup.setCreateTime(LocalDateTime.now());
        signup.setUpdateTime(LocalDateTime.now());
        this.save(signup);

        result.put("riskCheckPassed", true);
        result.put("riskReason", null);
        result.put("restriction", null);
        result.put("signupId", signup.getId());
        result.put("signup", signup);
        result.put("message", "报名成功");
        return result;
    }

    @Override
    public boolean cancelSignup(Long id, Long userId) {
        WorkSignup signup = this.getById(id);
        if (signup == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (!signup.getUserId().equals(userId)) {
            throw new BusinessException("无权取消他人报名");
        }
        if (!"pending".equals(signup.getStatus())) {
            throw new BusinessException("当前状态无法取消报名");
        }
        signup.setStatus("cancelled");
        signup.setUpdateTime(LocalDateTime.now());
        return this.updateById(signup);
    }

    private String getRiskTypeText(String type) {
        Map<String, String> texts = new HashMap<>();
        texts.put("miss_appointment", "爽约");
        texts.put("site_conflict", "现场冲突");
        texts.put("fake_cert", "证件造假");
        texts.put("other", "其他");
        return texts.getOrDefault(type, type);
    }

    private String getRiskStatusText(String status) {
        Map<String, String> texts = new HashMap<>();
        texts.put("active", "生效中");
        texts.put("appealing", "申诉审核中");
        texts.put("resolved", "已解除");
        texts.put("rejected", "申诉驳回");
        return texts.getOrDefault(status, status);
    }
}

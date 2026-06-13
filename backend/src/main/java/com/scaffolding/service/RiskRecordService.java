package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.RiskRecord;

import java.util.List;

/**
 * 风险记录服务接口
 * 
 * @author scaffolding
 */
public interface RiskRecordService extends IService<RiskRecord> {

    /**
     * 分页查询风险记录
     */
    Page<RiskRecord> pageQuery(Long current, Long size, Long userId, String riskType, String status, String riskLevel);

    /**
     * 查询用户的有效风险记录
     */
    List<RiskRecord> getActiveRiskRecords(Long userId);

    /**
     * 检查用户是否可以报名（高风险岗位）
     */
    boolean canApplyForHighRiskWork(Long userId);

    /**
     * 获取用户报名限制信息
     */
    RiskRecord getApplyRestriction(Long userId);

    /**
     * 添加风险记录
     */
    RiskRecord addRiskRecord(RiskRecord riskRecord);

    /**
     * 解除风险限制
     */
    boolean resolveRiskRecord(Long id, Long operatorId, String operatorName);
}

package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.RiskAppeal;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险申诉服务接口
 * 
 * @author scaffolding
 */
public interface RiskAppealService extends IService<RiskAppeal> {

    /**
     * 分页查询申诉列表
     */
    Page<RiskAppeal> pageQuery(Long current, Long size, Long userId, Long riskRecordId, String status);

    /**
     * 查询用户的申诉记录
     */
    List<RiskAppeal> getAppealsByUserId(Long userId);

    /**
     * 提交申诉
     */
    RiskAppeal submitAppeal(RiskAppeal appeal);

    /**
     * 用工企业复核
     */
    RiskAppeal enterpriseReview(Long appealId, Long reviewerId, String reviewerName, String comment, boolean approved);

    /**
     * 平台运营复核
     */
    RiskAppeal platformReview(Long appealId, Long reviewerId, String reviewerName, String comment, boolean approved, LocalDateTime nextAppealTime);

    /**
     * 检查用户是否可以提交申诉
     */
    boolean canSubmitAppeal(Long riskRecordId, Long userId);

    /**
     * 获取下次可申诉时间
     */
    LocalDateTime getNextAppealTime(Long riskRecordId, Long userId);
}

package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 风险申诉实体类
 * 
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("risk_appeal")
public class RiskAppeal extends BaseEntity {

    /**
     * 风险记录ID
     */
    private Long riskRecordId;

    /**
     * 申诉人ID
     */
    private Long userId;

    /**
     * 申诉人姓名
     */
    private String userName;

    /**
     * 申诉理由
     */
    private String appealReason;

    /**
     * 证明材料（逗号分隔的文件ID）
     */
    private String evidenceFiles;

    /**
     * 状态（pending-待审核，approved-申诉通过，rejected-申诉驳回，reviewing-企业复核中）
     */
    private String status;

    /**
     * 用工企业复核人ID
     */
    private Long enterpriseReviewUserId;

    /**
     * 用工企业复核人姓名
     */
    private String enterpriseReviewUserName;

    /**
     * 用工企业复核意见
     */
    private String enterpriseReviewComment;

    /**
     * 用工企业复核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime enterpriseReviewTime;

    /**
     * 平台运营复核人ID
     */
    private Long platformReviewUserId;

    /**
     * 平台运营复核人姓名
     */
    private String platformReviewUserName;

    /**
     * 平台运营复核意见
     */
    private String platformReviewComment;

    /**
     * 平台运营复核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime platformReviewTime;

    /**
     * 最终复核意见
     */
    private String finalComment;

    /**
     * 下次可申诉时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime nextAppealTime;
}

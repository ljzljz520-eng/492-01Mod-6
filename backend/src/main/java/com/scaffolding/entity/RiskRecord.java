package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 风险记录实体类
 * 
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("risk_record")
public class RiskRecord extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 风险类型（miss_appointment-爽约，site_conflict-现场冲突，fake_cert-证件造假，other-其他）
     */
    private String riskType;

    /**
     * 风险等级（restricted-受限，banned-禁止）
     */
    private String riskLevel;

    /**
     * 风险描述
     */
    private String description;

    /**
     * 证据文件（逗号分隔的文件ID）
     */
    private String evidenceFiles;

    /**
     * 上报人ID
     */
    private Long reportUserId;

    /**
     * 上报人姓名
     */
    private String reportUserName;

    /**
     * 状态（active-生效，appealing-申诉中，resolved-已解除，rejected-申诉驳回）
     */
    private String status;

    /**
     * 限制结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime restrictEndTime;

    /**
     * 下次可申诉时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime nextAppealTime;
}

package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工作报名实体类
 * 
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_signup")
public class WorkSignup extends BaseEntity {

    /**
     * 工作ID
     */
    private Long workId;

    /**
     * 工作名称
     */
    private String workName;

    /**
     * 报名用户ID
     */
    private Long userId;

    /**
     * 报名用户姓名
     */
    private String userName;

    /**
     * 报名状态（pending-待审核，approved-已通过，rejected-已拒绝，cancelled-已取消）
     */
    private String status;

    /**
     * 风险检查是否通过（0-否，1-是）
     */
    private Integer riskCheckPassed;

    /**
     * 风险限制原因
     */
    private String riskReason;
}

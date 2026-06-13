package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    /**
     * 用户名（账号）
     */
    private String username;

    /**
     * 密码（不加密）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 风险等级（normal-正常，restricted-受限，banned-禁止）
     */
    private String riskLevel;

    /**
     * 受限原因
     */
    private String restrictReason;

    /**
     * 受限时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime restrictTime;
}

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scaffolding_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scaffolding_db`;

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 工作管理表
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_name` varchar(100) NOT NULL COMMENT '工作名称',
  `work_content` text COMMENT '工作内容',
  `work_status` varchar(20) DEFAULT 'pending' COMMENT '工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `work_time` datetime DEFAULT NULL COMMENT '工作时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级（low-低，normal-普通，high-高，urgent-紧急）',
  `is_high_risk` tinyint(1) DEFAULT '0' COMMENT '是否高风险岗位（0-否，1-是）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_work_time` (`work_time`),
  KEY `idx_priority` (`priority`),
  KEY `idx_is_high_risk` (`is_high_risk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作管理表';

-- 工作报名表
DROP TABLE IF EXISTS `work_signup`;
CREATE TABLE `work_signup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_id` bigint(20) NOT NULL COMMENT '工作ID',
  `work_name` varchar(100) DEFAULT NULL COMMENT '工作名称',
  `user_id` bigint(20) NOT NULL COMMENT '报名用户ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '报名用户姓名',
  `status` varchar(20) DEFAULT 'pending' COMMENT '报名状态（pending-待审核，approved-已通过，rejected-已拒绝，cancelled-已取消）',
  `risk_check_passed` tinyint(1) DEFAULT '1' COMMENT '风险检查是否通过（0-否，1-是）',
  `risk_reason` varchar(500) DEFAULT NULL COMMENT '风险限制原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_id` (`work_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作报名表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（不加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `risk_level` varchar(20) DEFAULT 'normal' COMMENT '风险等级（normal-正常，restricted-受限，banned-禁止）',
  `restrict_reason` varchar(500) DEFAULT NULL COMMENT '受限原因',
  `restrict_time` datetime DEFAULT NULL COMMENT '受限时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 风险记录表
DROP TABLE IF EXISTS `risk_record`;
CREATE TABLE `risk_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户姓名',
  `risk_type` varchar(50) NOT NULL COMMENT '风险类型（miss_appointment-爽约，site_conflict-现场冲突，fake_cert-证件造假，other-其他）',
  `risk_level` varchar(20) NOT NULL COMMENT '风险等级（restricted-受限，banned-禁止）',
  `description` varchar(1000) DEFAULT NULL COMMENT '风险描述',
  `evidence_files` varchar(1000) DEFAULT NULL COMMENT '证据文件（逗号分隔的文件ID）',
  `report_user_id` bigint(20) DEFAULT NULL COMMENT '上报人ID',
  `report_user_name` varchar(50) DEFAULT NULL COMMENT '上报人姓名',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态（active-生效，appealing-申诉中，resolved-已解除，rejected-申诉驳回）',
  `restrict_end_time` datetime DEFAULT NULL COMMENT '限制结束时间',
  `next_appeal_time` datetime DEFAULT NULL COMMENT '下次可申诉时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_risk_type` (`risk_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险记录表';

-- 风险申诉表
DROP TABLE IF EXISTS `risk_appeal`;
CREATE TABLE `risk_appeal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `risk_record_id` bigint(20) NOT NULL COMMENT '风险记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '申诉人ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '申诉人姓名',
  `appeal_reason` varchar(1000) NOT NULL COMMENT '申诉理由',
  `evidence_files` varchar(1000) DEFAULT NULL COMMENT '证明材料（逗号分隔的文件ID）',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态（pending-待审核，approved-申诉通过，rejected-申诉驳回，reviewing-企业复核中）',
  `enterprise_review_user_id` bigint(20) DEFAULT NULL COMMENT '用工企业复核人ID',
  `enterprise_review_user_name` varchar(50) DEFAULT NULL COMMENT '用工企业复核人姓名',
  `enterprise_review_comment` varchar(500) DEFAULT NULL COMMENT '用工企业复核意见',
  `enterprise_review_time` datetime DEFAULT NULL COMMENT '用工企业复核时间',
  `platform_review_user_id` bigint(20) DEFAULT NULL COMMENT '平台运营复核人ID',
  `platform_review_user_name` varchar(50) DEFAULT NULL COMMENT '平台运营复核人姓名',
  `platform_review_comment` varchar(500) DEFAULT NULL COMMENT '平台运营复核意见',
  `platform_review_time` datetime DEFAULT NULL COMMENT '平台运营复核时间',
  `final_comment` varchar(500) DEFAULT NULL COMMENT '最终复核意见',
  `next_appeal_time` datetime DEFAULT NULL COMMENT '下次可申诉时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_risk_record_id` (`risk_record_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险申诉表';

-- 插入默认admin账号
INSERT INTO `user` (`username`, `password`, `nickname`) VALUES ('admin', '123456', '管理员');

SET FOREIGN_KEY_CHECKS = 1;

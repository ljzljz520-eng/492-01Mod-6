package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.WorkSignup;

import java.util.Map;

/**
 * 工作报名服务接口
 * 
 * @author scaffolding
 */
public interface WorkSignupService extends IService<WorkSignup> {

    /**
     * 分页查询报名记录
     */
    Page<WorkSignup> pageQuery(Long current, Long size, Long workId, Long userId, String status);

    /**
     * 报名工作（含风险检查）
     */
    Map<String, Object> signupWork(Long workId, Long userId, String userName);

    /**
     * 取消报名
     */
    boolean cancelSignup(Long id, Long userId);
}

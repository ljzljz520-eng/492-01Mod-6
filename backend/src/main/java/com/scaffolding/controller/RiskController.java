package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.RiskAppeal;
import com.scaffolding.entity.RiskRecord;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.service.RiskAppealService;
import com.scaffolding.service.RiskRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 风险名单与申诉管理控制器
 * 
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/risk")
@Api(tags = "风险名单与申诉管理")
public class RiskController {

    @Autowired
    private RiskRecordService riskRecordService;

    @Autowired
    private RiskAppealService riskAppealService;

    // ==================== 风险记录相关接口 ====================

    @GetMapping("/record/page")
    @ApiOperation("分页查询风险记录")
    public Result<PageResult<RiskRecord>> pageRecords(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String riskType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String riskLevel) {
        Page<RiskRecord> page = riskRecordService.pageQuery(current, size, userId, riskType, status, riskLevel);
        PageResult<RiskRecord> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
        return Result.success(pageResult);
    }

    @GetMapping("/record/user/{userId}")
    @ApiOperation("查询用户的有效风险记录")
    public Result<List<RiskRecord>> getUserActiveRecords(@PathVariable Long userId) {
        return Result.success(riskRecordService.getActiveRiskRecords(userId));
    }

    @GetMapping("/record/{id}")
    @ApiOperation("根据ID查询风险记录")
    public Result<RiskRecord> getRecordById(@PathVariable Long id) {
        RiskRecord record = riskRecordService.getById(id);
        if (record == null) {
            return Result.error("风险记录不存在");
        }
        return Result.success(record);
    }

    @PostMapping("/record")
    @ApiOperation("添加风险记录")
    public Result<RiskRecord> addRecord(@RequestBody RiskRecord riskRecord) {
        try {
            if (riskRecord.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (!StringUtils.hasText(riskRecord.getRiskType())) {
                return Result.error("风险类型不能为空");
            }
            if (!StringUtils.hasText(riskRecord.getRiskLevel())) {
                return Result.error("风险等级不能为空");
            }
            RiskRecord saved = riskRecordService.addRiskRecord(riskRecord);
            return Result.success("添加成功", saved);
        } catch (Exception e) {
            log.error("添加风险记录失败", e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @PutMapping("/record/{id}/resolve")
    @ApiOperation("解除风险限制")
    public Result<?> resolveRecord(@PathVariable Long id, @RequestBody ResolveRequest request) {
        try {
            riskRecordService.resolveRiskRecord(id, request.getOperatorId(), request.getOperatorName());
            return Result.success("解除成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("解除风险限制失败", e);
            return Result.error("解除失败：" + e.getMessage());
        }
    }

    @GetMapping("/check-apply/{userId}")
    @ApiOperation("检查用户是否可报名高风险岗位")
    public Result<Map<String, Object>> checkApplyForHighRisk(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        boolean canApply = riskRecordService.canApplyForHighRiskWork(userId);
        result.put("canApply", canApply);
        if (!canApply) {
            RiskRecord restriction = riskRecordService.getApplyRestriction(userId);
            result.put("restriction", restriction);
        }
        return Result.success(result);
    }

    // ==================== 申诉相关接口 ====================

    @GetMapping("/appeal/page")
    @ApiOperation("分页查询申诉列表")
    public Result<PageResult<RiskAppeal>> pageAppeals(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long riskRecordId,
            @RequestParam(required = false) String status) {
        Page<RiskAppeal> page = riskAppealService.pageQuery(current, size, userId, riskRecordId, status);
        PageResult<RiskAppeal> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
        return Result.success(pageResult);
    }

    @GetMapping("/appeal/user/{userId}")
    @ApiOperation("查询用户的申诉记录")
    public Result<List<RiskAppeal>> getUserAppeals(@PathVariable Long userId) {
        return Result.success(riskAppealService.getAppealsByUserId(userId));
    }

    @GetMapping("/appeal/{id}")
    @ApiOperation("根据ID查询申诉详情")
    public Result<RiskAppeal> getAppealById(@PathVariable Long id) {
        RiskAppeal appeal = riskAppealService.getById(id);
        if (appeal == null) {
            return Result.error("申诉记录不存在");
        }
        return Result.success(appeal);
    }

    @PostMapping("/appeal")
    @ApiOperation("提交申诉")
    public Result<RiskAppeal> submitAppeal(@RequestBody RiskAppeal appeal) {
        try {
            if (appeal.getRiskRecordId() == null) {
                return Result.error("风险记录ID不能为空");
            }
            if (appeal.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (!StringUtils.hasText(appeal.getAppealReason())) {
                return Result.error("申诉理由不能为空");
            }
            RiskAppeal saved = riskAppealService.submitAppeal(appeal);
            return Result.success("申诉提交成功", saved);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("提交申诉失败", e);
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    @GetMapping("/appeal/can-submit")
    @ApiOperation("检查是否可以提交申诉")
    public Result<Map<String, Object>> canSubmitAppeal(
            @RequestParam Long riskRecordId,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        boolean canSubmit = riskAppealService.canSubmitAppeal(riskRecordId, userId);
        result.put("canSubmit", canSubmit);
        if (!canSubmit) {
            LocalDateTime nextTime = riskAppealService.getNextAppealTime(riskRecordId, userId);
            result.put("nextAppealTime", nextTime);
        }
        return Result.success(result);
    }

    @PutMapping("/appeal/{id}/enterprise-review")
    @ApiOperation("用工企业复核申诉")
    public Result<RiskAppeal> enterpriseReview(
            @PathVariable Long id,
            @RequestBody EnterpriseReviewRequest request) {
        try {
            if (request.getReviewerId() == null) {
                return Result.error("复核人ID不能为空");
            }
            RiskAppeal result = riskAppealService.enterpriseReview(
                    id, request.getReviewerId(), request.getReviewerName(),
                    request.getComment(), request.isApproved());
            return Result.success("企业复核完成", result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("企业复核失败", e);
            return Result.error("复核失败：" + e.getMessage());
        }
    }

    @PutMapping("/appeal/{id}/platform-review")
    @ApiOperation("平台运营复核申诉")
    public Result<RiskAppeal> platformReview(
            @PathVariable Long id,
            @RequestBody PlatformReviewRequest request) {
        try {
            if (request.getReviewerId() == null) {
                return Result.error("复核人ID不能为空");
            }
            RiskAppeal result = riskAppealService.platformReview(
                    id, request.getReviewerId(), request.getReviewerName(),
                    request.getComment(), request.isApproved(), request.getNextAppealTime());
            return Result.success("平台复核完成", result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("平台复核失败", e);
            return Result.error("复核失败：" + e.getMessage());
        }
    }

    // ==================== 请求对象 ====================

    @Data
    public static class ResolveRequest {
        private Long operatorId;
        private String operatorName;
    }

    @Data
    public static class EnterpriseReviewRequest {
        private Long reviewerId;
        private String reviewerName;
        private String comment;
        private boolean approved;
    }

    @Data
    public static class PlatformReviewRequest {
        private Long reviewerId;
        private String reviewerName;
        private String comment;
        private boolean approved;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime nextAppealTime;
    }
}

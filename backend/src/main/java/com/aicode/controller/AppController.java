package com.aicode.controller;

import com.aicode.common.exception.BusinessException;
import com.aicode.common.result.Result;
import com.aicode.common.util.SecurityUtil;
import com.aicode.entity.App;
import com.aicode.service.AppService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "应用管理接口")
@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    @Operation(summary = "创建应用")
    @PostMapping
    public Result<App> createApp(@RequestBody CreateAppRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(appService.createApp(userId, req.getTitle(), req.getDescription(), req.getPrompt()));
    }

    @Operation(summary = "获取我的应用列表")
    @GetMapping("/my")
    public Result<Page<App>> myApps(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(appService.listUserApps(userId, page, size));
    }

    @Operation(summary = "获取公开应用列表")
    @GetMapping("/public")
    public Result<Page<App>> publicApps(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return Result.success(appService.listPublicApps(page, size));
    }

    @Operation(summary = "获取应用详情")
    @GetMapping("/{id}")
    public Result<App> getApp(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        App app = appService.getAppById(id);
        // Public apps or owner can view
        if (!app.getUserId().equals(userId) && !"PUBLISHED".equals(app.getStatus())) {
            throw BusinessException.forbidden();
        }
        return Result.success(app);
    }

    @Operation(summary = "更新应用代码")
    @PutMapping("/{id}")
    public Result<App> updateApp(@PathVariable Long id, @RequestBody UpdateAppRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(appService.updateAppCode(id, userId,
                req.getHtmlContent(), req.getCssContent(), req.getJsContent(), req.getTitle()));
    }

    @Operation(summary = "删除应用")
    @DeleteMapping("/{id}")
    public Result<Void> deleteApp(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        appService.deleteApp(id, userId);
        return Result.success();
    }

    @Data
    public static class CreateAppRequest {
        private String title;
        private String description;
        private String prompt;
    }

    @Data
    public static class UpdateAppRequest {
        private String title;
        private String htmlContent;
        private String cssContent;
        private String jsContent;
    }
}

package com.aicode.controller;

import com.aicode.common.exception.BusinessException;
import com.aicode.common.result.Result;
import com.aicode.common.util.SecurityUtil;
import com.aicode.entity.AiLog;
import com.aicode.entity.App;
import com.aicode.entity.User;
import com.aicode.mapper.AiLogMapper;
import com.aicode.mapper.AppMapper;
import com.aicode.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "管理员接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final AppMapper appMapper;
    private final AiLogMapper aiLogMapper;

    // ===== Users =====

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(required = false) String keyword) {
        requireAdmin();
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            query.like(User::getUsername, keyword).or().like(User::getEmail, keyword);
        }
        Page<User> result = userMapper.selectPage(new Page<>(page, size), query);
        // Clear passwords
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        requireAdmin();
        User user = userMapper.selectById(id);
        if (user == null)
            throw BusinessException.notFound("用户");
        user.setStatus(body.getOrDefault("status", "active"));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        requireAdmin();
        userMapper.deleteById(id);
        return Result.success();
    }

    // ===== Apps =====

    @Operation(summary = "所有应用列表（管理员）")
    @GetMapping("/apps")
    public Result<Page<App>> listAllApps(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(required = false) String status) {
        requireAdmin();
        LambdaQueryWrapper<App> query = new LambdaQueryWrapper<App>()
                .orderByDesc(App::getCreatedAt);
        if (status != null && !status.isBlank()) {
            query.eq(App::getStatus, status);
        }
        return Result.success(appMapper.selectPage(new Page<>(page, size), query));
    }

    @Operation(summary = "设置/取消精选")
    @PutMapping("/apps/{id}/featured")
    public Result<Void> setFeatured(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        requireAdmin();
        App app = appMapper.selectById(id);
        if (app == null)
            throw BusinessException.notFound("应用");
        app.setIsFeatured(body.getOrDefault("featured", false));
        app.setUpdatedAt(LocalDateTime.now());
        appMapper.updateById(app);
        return Result.success();
    }

    @Operation(summary = "删除应用（管理员）")
    @DeleteMapping("/apps/{id}")
    public Result<Void> deleteApp(@PathVariable Long id) {
        requireAdmin();
        appMapper.deleteById(id);
        return Result.success();
    }

    // ===== Stats & Monitoring =====

    @Operation(summary = "仪表盘统计数据")
    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> statsOverview() {
        requireAdmin();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalApps", appMapper.selectCount(null));
        stats.put("publishedApps", appMapper.selectCount(
                new LambdaQueryWrapper<App>().eq(App::getStatus, "PUBLISHED")));
        stats.put("totalAiCalls", aiLogMapper.selectCount(null));
        // Today's stats
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        stats.put("todayUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreatedAt, todayStart)));
        stats.put("todayApps", appMapper.selectCount(
                new LambdaQueryWrapper<App>().ge(App::getCreatedAt, todayStart)));
        stats.put("todayAiCalls", aiLogMapper.selectCount(
                new LambdaQueryWrapper<AiLog>().ge(AiLog::getCreatedAt, todayStart)));
        return Result.success(stats);
    }

    @Operation(summary = "AI调用日志")
    @GetMapping("/stats/ai-logs")
    public Result<Page<AiLog>> aiLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin();
        return Result.success(aiLogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<AiLog>().orderByDesc(AiLog::getCreatedAt)));
    }

    @Operation(summary = "精选应用列表（公开）")
    @GetMapping("/featured")
    public Result<List<App>> featuredApps() {
        return Result.success(appMapper.selectList(
                new LambdaQueryWrapper<App>()
                        .eq(App::getIsFeatured, true)
                        .eq(App::getStatus, "PUBLISHED")
                        .orderByDesc(App::getCreatedAt)
                        .last("LIMIT 12")));
    }

    // ===== Admin check (whitelist-based for now) =====
    private void requireAdmin() {
        User currentUser = userMapper.selectById(SecurityUtil.getCurrentUserId());
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            throw BusinessException.forbidden();
        }
    }
}

package com.aicode.service;

import com.aicode.common.exception.BusinessException;
import com.aicode.entity.App;
import com.aicode.mapper.AppMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppMapper appMapper;

    public App createApp(Long userId, String title, String description, String prompt) {
        App app = new App();
        app.setUserId(userId);
        app.setTitle(title != null ? title : "新建应用");
        app.setDescription(description);
        app.setPrompt(prompt);
        app.setStatus("GENERATING");
        app.setIsFeatured(false);
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        appMapper.insert(app);
        return app;
    }

    public Page<App> listUserApps(Long userId, int page, int size) {
        return appMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<App>()
                        .eq(App::getUserId, userId)
                        .orderByDesc(App::getCreatedAt));
    }

    public Page<App> listPublicApps(int page, int size) {
        return appMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<App>()
                        .eq(App::getStatus, "PUBLISHED")
                        .orderByDesc(App::getCreatedAt));
    }

    public App getAppById(Long appId) {
        App app = appMapper.selectById(appId);
        if (app == null)
            throw BusinessException.notFound("应用");
        return app;
    }

    public App getAppByIdAndUser(Long appId, Long userId) {
        App app = getAppById(appId);
        if (!app.getUserId().equals(userId))
            throw BusinessException.forbidden();
        return app;
    }

    public App updateAppCode(Long appId, Long userId, String html, String css, String js, String title) {
        App app = getAppByIdAndUser(appId, userId);
        if (html != null)
            app.setHtmlContent(html);
        if (css != null)
            app.setCssContent(css);
        if (js != null)
            app.setJsContent(js);
        if (title != null)
            app.setTitle(title);
        app.setStatus("DONE");
        app.setUpdatedAt(LocalDateTime.now());
        appMapper.updateById(app);
        return app;
    }

    public App updateAppStatus(Long appId, String status, String html, String css, String js, String title) {
        App app = appMapper.selectById(appId);
        if (app == null)
            return null;
        if (status != null)
            app.setStatus(status);
        if (html != null)
            app.setHtmlContent(html);
        if (css != null)
            app.setCssContent(css);
        if (js != null)
            app.setJsContent(js);
        if (title != null)
            app.setTitle(title);
        app.setUpdatedAt(LocalDateTime.now());
        appMapper.updateById(app);
        return app;
    }

    public App update(Long appId, Long userId, Map<String, Object> fields) {
        App app = getAppByIdAndUser(appId, userId);
        if (fields.containsKey("title"))
            app.setTitle((String) fields.get("title"));
        if (fields.containsKey("htmlContent"))
            app.setHtmlContent((String) fields.get("htmlContent"));
        if (fields.containsKey("cssContent"))
            app.setCssContent((String) fields.get("cssContent"));
        if (fields.containsKey("jsContent"))
            app.setJsContent((String) fields.get("jsContent"));
        if (fields.containsKey("description"))
            app.setDescription((String) fields.get("description"));
        app.setUpdatedAt(LocalDateTime.now());
        appMapper.updateById(app);
        return app;
    }

    public void deleteApp(Long appId, Long userId) {
        App app = getAppByIdAndUser(appId, userId);
        appMapper.deleteById(app.getId());
    }
}

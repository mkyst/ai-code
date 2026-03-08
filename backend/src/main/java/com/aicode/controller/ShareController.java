package com.aicode.controller;

import com.aicode.common.exception.BusinessException;
import com.aicode.common.result.Result;
import com.aicode.common.util.SecurityUtil;
import com.aicode.entity.App;
import com.aicode.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "部署与分享接口")
@RestController
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @Operation(summary = "发布应用并生成分享链接")
    @PostMapping("/api/apps/{id}/publish")
    public Result<App> publishApp(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(shareService.publishApp(id, userId));
    }

    @Operation(summary = "Fork 应用")
    @PostMapping("/api/apps/{id}/fork")
    public Result<App> forkApp(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(shareService.forkApp(id, userId));
    }

    @Operation(summary = "下载源码 ZIP")
    @GetMapping("/api/apps/{id}/download")
    public void downloadZip(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Long userId = SecurityUtil.getCurrentUserId();
        App app = shareService.getAppForDownload(id, userId);
        byte[] zipData = shareService.buildZip(app);

        String filename = URLEncoder.encode(app.getTitle(), StandardCharsets.UTF_8) + ".zip";
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename);
        response.setContentLength(zipData.length);
        response.getOutputStream().write(zipData);
    }

    /**
     * Public share page — returns the full HTML for the shared app
     */
    @GetMapping(value = "/share/{token}", produces = MediaType.TEXT_HTML_VALUE)
    public String shareView(@PathVariable String token) {
        App app = shareService.getByShareToken(token);
        return shareService.buildPreviewHtml(app);
    }

    /**
     * Embed mode — same as share but in minimal wrapper
     */
    @GetMapping(value = "/embed/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String embedView(@PathVariable Long id) {
        App app = shareService.getAppForEmbed(id);
        if (!"PUBLISHED".equals(app.getStatus()))
            throw BusinessException.forbidden();
        return shareService.buildPreviewHtml(app);
    }
}

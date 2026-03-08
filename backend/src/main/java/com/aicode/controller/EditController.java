package com.aicode.controller;

import com.aicode.ai.AiEditService;
import com.aicode.common.result.Result;
import com.aicode.common.util.SecurityUtil;
import com.aicode.entity.AppVersion;
import com.aicode.entity.ChatMessage;
import com.aicode.service.AppVersionService;
import com.aicode.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "编辑与版本接口")
@RestController
@RequestMapping("/api/edit")
@RequiredArgsConstructor
public class EditController {

    private final AiEditService aiEditService;
    private final AppVersionService versionService;
    private final ChatMessageService chatMessageService;

    @Operation(summary = "AI修改代码 (SSE流式)")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter editStream(
            @RequestParam Long appId,
            @RequestParam String message,
            @RequestParam(required = false) String selectedElement) {
        Long userId = SecurityUtil.getCurrentUserId();
        return aiEditService.editWithAi(userId, appId, message, selectedElement);
    }

    @Operation(summary = "获取版本历史")
    @GetMapping("/versions/{appId}")
    public Result<List<AppVersion>> getVersions(@PathVariable Long appId) {
        return Result.success(versionService.getVersionHistory(appId));
    }

    @Operation(summary = "回滚到指定版本")
    @PostMapping("/versions/{versionId}/restore")
    public Result<AppVersion> restoreVersion(@PathVariable Long versionId) {
        AppVersion version = versionService.getVersion(versionId);
        if (version == null)
            return Result.error("版本不存在");
        // The actual restore is done on the frontend by updating app code
        return Result.success(version);
    }

    @Operation(summary = "获取对话历史")
    @GetMapping("/chat/{appId}")
    public Result<List<ChatMessage>> getChatHistory(@PathVariable Long appId) {
        return Result.success(chatMessageService.getHistory(appId));
    }

    @Operation(summary = "清除对话历史")
    @DeleteMapping("/chat/{appId}")
    public Result<Void> clearChatHistory(@PathVariable Long appId) {
        chatMessageService.clearHistory(appId);
        return Result.success();
    }

    @Data
    public static class EditRequest {
        private Long appId;
        private String message;
        private String selectedElement;
    }
}

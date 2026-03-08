package com.aicode.controller;

import com.aicode.ai.AiCodeGenerationService;
import com.aicode.common.result.Result;
import com.aicode.common.util.SecurityUtil;
import com.aicode.entity.App;
import com.aicode.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "AI生成接口")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiCodeGenerationService aiService;
    private final AppService appService;

    @Operation(summary = "流式AI代码生成 (SSE)")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGenerate(
            @RequestParam Long appId,
            @RequestParam String prompt) {
        Long userId = SecurityUtil.getCurrentUserId();
        return aiService.generateCode(userId, appId, prompt);
    }

    @Operation(summary = "创建应用并开始生成")
    @PostMapping("/generate")
    public Result<App> startGenerate(@RequestBody GenerateRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        // Create app record first
        App app = appService.createApp(userId, req.getTitle(), req.getDescription(), req.getPrompt());
        return Result.success(app);
    }

    @Data
    public static class GenerateRequest {
        @NotBlank(message = "提示词不能为空")
        private String prompt;
        private String title;
        private String description;
    }
}

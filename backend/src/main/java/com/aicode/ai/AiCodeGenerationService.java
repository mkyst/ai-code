package com.aicode.ai;

import com.aicode.ai.tools.CodeGenerationTools;
import com.aicode.service.AppService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCodeGenerationService {

    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final AppService appService;
    private final ObjectProvider<CodeGenerationTools> toolsProvider;

    public SseEmitter generateCode(Long userId, Long appId, String userPrompt) {
        SseEmitter emitter = new SseEmitter(300_000L);

        CompletableFuture.runAsync(() -> {
            CodeGenerationTools tools = toolsProvider.getObject();

            // SSE publisher
            tools.setEventPublisher(event -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name(event.type())
                            .data(event.data()));
                } catch (IOException e) {
                    log.warn("SSE send failed: {}", e.getMessage());
                }
            });

            // Build the assistant interface with tool calling via AiServices
            interface CodeAssistant {
                String chat(String message);
            }

            // Use non-streaming assistant for tool calling (tools need synchronous
            // execution)
            // then stream thinking messages separately
            try {
                sendSseEvent(emitter, "thinking", "{\"message\":\"🤔 AI正在分析需求，规划代码架构...\"}");

                // Build AiServices with tool calling (synchronous for proper tool execution)
                CodeAssistant assistant = AiServices.builder(CodeAssistant.class)
                        .streamingChatLanguageModel(streamingChatLanguageModel)
                        .tools(tools)
                        .build();

                // Run in thread to avoid blocking
                String result = assistant.chat(buildSystemPrompt(userPrompt));

                // Save to db
                appService.updateAppStatus(
                        appId, "DONE",
                        tools.getHtmlContent(),
                        tools.getCssContent(),
                        tools.getJsContent(),
                        tools.getAppTitle());

                sendSseEvent(emitter, "done",
                        "{\"appId\":" + appId +
                                ",\"title\":\"" + tools.getAppTitle().replace("\"", "\\\"") + "\"" +
                                ",\"message\":\"🎉 生成完成！\"}");
                emitter.complete();

            } catch (Exception e) {
                log.error("AI generation error", e);
                try {
                    sendSseEvent(emitter, "error",
                            "{\"message\":\"生成失败: " + e.getMessage().replace("\"", "'") + "\"}");
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }

    private String buildSystemPrompt(String userPrompt) {
        return """
                你是一个专业的前端代码生成专家。用户需要你根据需求生成一个完整的、美观的网页应用。

                要求：
                1. 必须调用工具函数来生成代码文件，不要只是描述要做什么
                2. 首先调用 setAppTitle 设置应用标题
                3. 然后依次调用 createHtmlFile、createCssFile、createJsFile 生成三个文件
                4. HTML要包含完整结构，CSS要现代化美观（使用渐变、动画、阴影等），JS要实现完整交互
                5. 最后调用 finishGeneration 标记完成
                6. 代码必须能独立运行，HTML文件需内嵌CSS和JS的引用关系
                7. 使用现代CSS特性（flex/grid布局、CSS变量、动画）
                8. 页面要有响应式设计，支持移动端
                9. 生成的代码要丰富、完整，不能是简陋的示例

                用户需求：%s
                """.formatted(userPrompt);
    }

    private void sendSseEvent(SseEmitter emitter, String name, String data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            log.warn("SSE send failed: {}", e.getMessage());
        }
    }
}

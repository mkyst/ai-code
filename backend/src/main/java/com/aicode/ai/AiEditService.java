package com.aicode.ai;

import com.aicode.ai.tools.CodeEditTools;
import com.aicode.entity.App;
import com.aicode.entity.ChatMessage;
import com.aicode.service.AppService;
import com.aicode.service.AppVersionService;
import com.aicode.service.ChatMessageService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiEditService {

    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final AppService appService;
    private final AppVersionService versionService;
    private final ChatMessageService chatMessageService;
    private final ObjectProvider<CodeEditTools> editToolsProvider;

    public SseEmitter editWithAi(Long userId, Long appId, String userMessage, String selectedElement) {
        SseEmitter emitter = new SseEmitter(300_000L);

        CompletableFuture.runAsync(() -> {
            try {
                App app = appService.getAppById(appId);

                // Save version snapshot before editing
                versionService.saveVersion(
                        appId,
                        app.getHtmlContent(),
                        app.getCssContent(),
                        app.getJsContent(),
                        app.getTitle(),
                        "编辑前快照");

                // Build edit tools (prototype scoped)
                CodeEditTools editTools = editToolsProvider.getObject();
                editTools.setCurrentCode(app.getHtmlContent(), app.getCssContent(), app.getJsContent());
                editTools.setEventPublisher(event -> {
                    try {
                        emitter.send(SseEmitter.event().name(event.type()).data(event.data()));
                    } catch (IOException e) {
                        log.warn("SSE send failed: {}", e.getMessage());
                    }
                });

                // Save user message to history
                chatMessageService.saveMessage(appId, userId, "user", userMessage);

                // Build conversation history
                List<ChatMessage> history = chatMessageService.getHistory(appId);

                // Build system prompt with current code context
                String systemPrompt = buildEditSystemPrompt(app, selectedElement);

                // Build AiServices with tools
                interface EditAssistant {
                    String chat(String message);
                }

                EditAssistant assistant = AiServices.builder(EditAssistant.class)
                        .streamingChatLanguageModel(streamingChatLanguageModel)
                        .tools(editTools)
                        .systemMessageProvider(memId -> systemPrompt)
                        .build();

                sendSse(emitter, "thinking", "{\"message\":\"🤔 AI正在分析修改方案...\"}");

                // Execute with full context message
                String contextualMessage = buildContextualMessage(userMessage, selectedElement, history);
                String result = assistant.chat(contextualMessage);

                // Save AI response to history
                chatMessageService.saveMessage(appId, userId, "assistant", result);

                // Apply changes to app
                String newHtml = editTools.getUpdatedHtml() != null ? editTools.getUpdatedHtml() : app.getHtmlContent();
                String newCss = editTools.getUpdatedCss() != null ? editTools.getUpdatedCss() : app.getCssContent();
                String newJs = editTools.getUpdatedJs() != null ? editTools.getUpdatedJs() : app.getJsContent();

                appService.updateAppStatus(appId, "DONE", newHtml, newCss, newJs, app.getTitle());

                // Save version after editing
                versionService.saveVersion(appId, newHtml, newCss, newJs, app.getTitle(), userMessage);

                sendSse(emitter, "done", "{\"appId\":" + appId + ",\"message\":\"✅ 修改完成\"}");
                emitter.complete();

            } catch (Exception e) {
                log.error("AI edit error", e);
                try {
                    sendSse(emitter, "error", "{\"message\":\"修改失败: " + e.getMessage().replace("\"", "'") + "\"}");
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }

    private String buildEditSystemPrompt(App app, String selectedElement) {
        String elementContext = (selectedElement != null && !selectedElement.isBlank())
                ? "\n\n用户当前选中的元素：\n```html\n" + selectedElement + "\n```\n请重点修改这个元素。"
                : "";

        return """
                你是一个专业的前端代码修改专家。你有当前应用的完整代码，需要根据用户需求精确修改。

                当前 HTML 代码：
                ```html
                %s
                ```

                当前 CSS 代码：
                ```css
                %s
                ```

                当前 JS 代码：
                ```js
                %s
                ```
                %s

                修改规则：
                1. 必须调用 updateHtmlFile、updateCssFile 或 updateJsFile 工具来提交修改后的完整代码
                2. 只修改用户要求的部分，保留其他代码不变
                3. 确保修改后的代码语法正确、功能完整
                4. 最后调用 finishEdit 告知修改完成，并用中文简洁说明修改了什么
                5. 代码必须是完整的文件内容（不是 diff），不能只输出修改部分
                """.formatted(
                truncate(app.getHtmlContent(), 3000),
                truncate(app.getCssContent(), 2000),
                truncate(app.getJsContent(), 2000),
                elementContext);
    }

    private String buildContextualMessage(String userMessage, String selectedElement,
            List<ChatMessage> history) {
        StringBuilder sb = new StringBuilder();
        // Include recent history for context
        if (!history.isEmpty()) {
            sb.append("【历史对话记录】\n");
            int start = Math.max(0, history.size() - 6); // last 3 turns
            for (int i = start; i < history.size(); i++) {
                ChatMessage m = history.get(i);
                sb.append(m.getRole().equals("user") ? "用户: " : "AI: ");
                sb.append(m.getContent()).append("\n");
            }
            sb.append("\n【本次需求】\n");
        }
        sb.append(userMessage);
        return sb.toString();
    }

    private String truncate(String s, int max) {
        if (s == null)
            return "";
        return s.length() > max ? s.substring(0, max) + "\n... (代码已截断)" : s;
    }

    private void sendSse(SseEmitter emitter, String name, String data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            log.warn("SSE send failed: {}", e.getMessage());
        }
    }
}

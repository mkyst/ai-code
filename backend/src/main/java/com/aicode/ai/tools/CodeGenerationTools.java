package com.aicode.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Code generation tools for LangChain4j Tool Calling.
 * Each instance is request-scoped to accumulate generated code.
 */
@Slf4j
@Component
@Scope("prototype")
public class CodeGenerationTools {

    @Getter
    private String htmlContent = "";
    @Getter
    private String cssContent = "";
    @Getter
    private String jsContent = "";
    @Getter
    private String appTitle = "AI生成应用";
    @Getter
    private boolean finished = false;

    // SSE event publisher callback (set before use)
    private Consumer<SseEvent> eventPublisher;

    public void setEventPublisher(Consumer<SseEvent> publisher) {
        this.eventPublisher = publisher;
    }

    @Tool("创建HTML文件内容，包含页面结构。请生成完整的、美观的HTML代码。")
    public String createHtmlFile(String content) {
        log.info("Tool called: createHtmlFile, length={}", content.length());
        this.htmlContent = content;
        publishEvent("tool_call",
                "{\"tool\":\"createHtmlFile\",\"message\":\"✅ HTML文件已生成\",\"length\":" + content.length() + "}");
        publishEvent("code", "{\"type\":\"html\",\"content\":" + escapeJson(content) + "}");
        return "HTML文件创建成功，共" + content.length() + "字符";
    }

    @Tool("创建CSS文件内容，包含样式定义。请生成现代化、美观的CSS样式，使用渐变、动画等效果。")
    public String createCssFile(String content) {
        log.info("Tool called: createCssFile, length={}", content.length());
        this.cssContent = content;
        publishEvent("tool_call",
                "{\"tool\":\"createCssFile\",\"message\":\"✅ CSS文件已生成\",\"length\":" + content.length() + "}");
        publishEvent("code", "{\"type\":\"css\",\"content\":" + escapeJson(content) + "}");
        return "CSS文件创建成功，共" + content.length() + "字符";
    }

    @Tool("创建JavaScript文件内容，包含交互逻辑。请生成完整的、功能丰富的JS代码。")
    public String createJsFile(String content) {
        log.info("Tool called: createJsFile, length={}", content.length());
        this.jsContent = content;
        publishEvent("tool_call",
                "{\"tool\":\"createJsFile\",\"message\":\"✅ JavaScript文件已生成\",\"length\":" + content.length() + "}");
        publishEvent("code", "{\"type\":\"js\",\"content\":" + escapeJson(content) + "}");
        return "JavaScript文件创建成功，共" + content.length() + "字符";
    }

    @Tool("设置应用的标题名称")
    public String setAppTitle(String title) {
        log.info("Tool called: setAppTitle, title={}", title);
        this.appTitle = title;
        publishEvent("tool_call", "{\"tool\":\"setAppTitle\",\"message\":\"📝 应用标题：" + title + "\"}");
        return "标题设置成功: " + title;
    }

    @Tool("标记代码生成完成，所有文件均已创建完毕")
    public String finishGeneration() {
        log.info("Tool called: finishGeneration");
        this.finished = true;
        publishEvent("tool_call", "{\"tool\":\"finishGeneration\",\"message\":\"🎉 代码生成完成！\"}");
        return "生成完成";
    }

    private void publishEvent(String type, String data) {
        if (eventPublisher != null) {
            eventPublisher.accept(new SseEvent(type, data));
        }
    }

    private String escapeJson(String s) {
        if (s == null)
            return "null";
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    public record SseEvent(String type, String data) {
    }
}

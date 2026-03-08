package com.aicode.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Tools for editing existing code. Prototype-scoped per request.
 */
@Slf4j
@Component
@Scope("prototype")
public class CodeEditTools {

    @Getter
    private String updatedHtml;
    @Getter
    private String updatedCss;
    @Getter
    private String updatedJs;
    @Getter
    private String editSummary;

    private String currentHtml;
    private String currentCss;
    private String currentJs;

    private Consumer<CodeGenerationTools.SseEvent> eventPublisher;

    public void setCurrentCode(String html, String css, String js) {
        this.currentHtml = html;
        this.currentCss = css;
        this.currentJs = js;
    }

    public void setEventPublisher(Consumer<CodeGenerationTools.SseEvent> publisher) {
        this.eventPublisher = publisher;
    }

    @Tool("更新HTML文件内容。传入修改后的完整HTML代码（不是diff，是完整文件）。")
    public String updateHtmlFile(String content) {
        log.info("Tool: updateHtmlFile, length={}", content.length());
        this.updatedHtml = content;
        publish("tool_call",
                "{\"tool\":\"updateHtmlFile\",\"message\":\"✅ HTML已更新\",\"length\":" + content.length() + "}");
        publish("code", "{\"type\":\"html\",\"content\":" + escapeJson(content) + "}");
        return "HTML更新成功";
    }

    @Tool("更新CSS文件内容。传入修改后的完整CSS代码（不是diff，是完整文件）。")
    public String updateCssFile(String content) {
        log.info("Tool: updateCssFile, length={}", content.length());
        this.updatedCss = content;
        publish("tool_call",
                "{\"tool\":\"updateCssFile\",\"message\":\"✅ CSS已更新\",\"length\":" + content.length() + "}");
        publish("code", "{\"type\":\"css\",\"content\":" + escapeJson(content) + "}");
        return "CSS更新成功";
    }

    @Tool("更新JavaScript文件内容。传入修改后的完整JS代码（不是diff，是完整文件）。")
    public String updateJsFile(String content) {
        log.info("Tool: updateJsFile, length={}", content.length());
        this.updatedJs = content;
        publish("tool_call", "{\"tool\":\"updateJsFile\",\"message\":\"✅ JS已更新\",\"length\":" + content.length() + "}");
        publish("code", "{\"type\":\"js\",\"content\":" + escapeJson(content) + "}");
        return "JS更新成功";
    }

    @Tool("确认修改完成，并简洁说明本次修改了什么（用中文）")
    public String finishEdit(String summary) {
        log.info("Tool: finishEdit, summary={}", summary);
        this.editSummary = summary;
        publish("tool_call", "{\"tool\":\"finishEdit\",\"message\":\"✅ " + summary.replace("\"", "'") + "\"}");
        return "修改完成: " + summary;
    }

    private void publish(String type, String data) {
        if (eventPublisher != null) {
            eventPublisher.accept(new CodeGenerationTools.SseEvent(type, data));
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
}

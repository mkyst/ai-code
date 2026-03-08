package com.aicode.service;

import com.aicode.entity.ChatMessage;
import com.aicode.mapper.ChatMessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    public void saveMessage(Long appId, Long userId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setAppId(appId);
        msg.setUserId(userId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(msg);
    }

    public List<ChatMessage> getHistory(Long appId) {
        return chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getAppId, appId)
                        .orderByAsc(ChatMessage::getCreatedAt)
                        .last("LIMIT 20") // Keep last 20 messages for context
        );
    }

    public void clearHistory(Long appId) {
        chatMessageMapper.delete(
                new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getAppId, appId));
    }
}

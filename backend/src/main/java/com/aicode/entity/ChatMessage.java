package com.aicode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_messages")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appId;
    private Long userId;
    private String role; // user / assistant
    private String content;
    private LocalDateTime createdAt;
}

package com.aicode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_logs")
public class AiLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long appId;
    private String model;
    private Integer promptTokens;
    private Integer completionTokens;
    private Double cost;
    private LocalDateTime createdAt;
}

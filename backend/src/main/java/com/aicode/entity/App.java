package com.aicode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("apps")
public class App {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String prompt;
    private String htmlContent;
    private String cssContent;
    private String jsContent;
    private String status; // GENERATING, DONE, PUBLISHED
    private String coverUrl;
    private String shareUrl;
    private Boolean isFeatured;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

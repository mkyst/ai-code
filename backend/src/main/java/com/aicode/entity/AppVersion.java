package com.aicode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("app_versions")
public class AppVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appId;
    private Integer versionNum;
    private String htmlContent;
    private String cssContent;
    private String jsContent;
    private String title;
    private String changeDesc; // 本次修改描述
    private LocalDateTime createdAt;
}

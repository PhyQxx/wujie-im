package com.wujie.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("announcement_read")
public class AnnouncementRead {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long announcementId;
    private Long userId;
    private LocalDateTime readTime;
}

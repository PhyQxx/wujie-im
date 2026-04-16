package com.wujie.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("robot")
public class Robot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String avatar;
    private String description;
    private String type;
    private Long ownerId;
    private String status;
    private String responseMode;
    private Integer contextSize;
    private String groupWhitelist;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}

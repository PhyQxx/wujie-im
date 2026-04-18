package com.wujie.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("group_info")
public class GroupInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String avatar;
    private String announcement;
    private String type;
    private Long ownerId;
    private Integer needAudit;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
    @TableField(exist = false)
    private String ownerName;
    @TableField(exist = false)
    private Integer memberCount;
}

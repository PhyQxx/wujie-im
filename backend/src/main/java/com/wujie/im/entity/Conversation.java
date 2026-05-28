package com.wujie.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.wujie.im.entity.GroupInfo;
import com.wujie.im.entity.User;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("type")
    private String type;
    @TableField("type_id")
    private Long typeId;
    @TableField("user_id")
    private Long userId;
    @TableField("last_message_id")
    private Long lastMessageId;
    @TableField("last_message_content")
    private String lastMessageContent;
    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;
    @TableField("unread_count")
    private Integer unreadCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
    @TableField(exist = false)
    private User targetUser;
    @TableField(exist = false)
    private GroupInfo groupInfo;
}

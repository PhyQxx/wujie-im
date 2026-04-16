package com.wujie.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("robot_rule")
public class RobotRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long robotId;
    private String ruleType;
    private String keyword;
    private String pattern;
    private String webhookUrl;
    private String replyContent;
    private String replyType;
    private Integer priority;
    private Integer enabled;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}

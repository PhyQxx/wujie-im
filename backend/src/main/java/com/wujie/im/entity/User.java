package com.wujie.im.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    /** pnkx 统一登录ID（OIDC sub），首次 SSO 登录 JIT 建号写入 */
    private String ssoId;
    private String password;
    private String phone;
    private String email;
    private String role;
    private String userType;
    private Integer status;
    private String userStatus;
    private LocalDateTime lastActiveTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    // 来自 user_profile 表，非持久化
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String signature;
}

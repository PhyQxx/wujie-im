package com.wujie.im.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.common.Result;
import com.wujie.im.entity.*;
import com.wujie.im.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminUserMapper adminUserMapper;
    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GroupInfoMapper groupInfoMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private RobotMapper robotMapper;
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    @Autowired
    private AiConfigMapper aiConfigMapper;
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    @Autowired
    private ConversationMapper conversationMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ==================== 认证 ====================
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        AdminUser admin = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, params.get("username"))
        );
        if (admin == null || !encoder.matches(params.get("password"), admin.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("role", admin.getRole());
        return Result.success(result);
    }

    @GetMapping("/check")
    public Result<Boolean> checkAdmin() {
        // 管理员表有记录则为管理员
        long count = adminUserMapper.selectCount(null);
        return Result.success(count > 0);
    }

    // ==================== 统计数据看板 ====================
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userMapper.selectCount(null);
        long totalGroups = groupInfoMapper.selectCount(null);
        long totalMessages = messageMapper.selectCount(null);
        long totalRobots = robotMapper.selectCount(null);

        // 今日新增
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, today)
        );
        long todayMessages = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>().ge(Message::getCreateTime, today)
        );

        stats.put("totalUsers", totalUsers);
        stats.put("totalGroups", totalGroups);
        stats.put("totalMessages", totalMessages);
        stats.put("totalRobots", totalRobots);
        stats.put("todayUsers", todayUsers);
        stats.put("todayMessages", todayMessages);

        return Result.success(stats);
    }

    // ==================== 用户管理 ====================
    @GetMapping("/users")
    public Result<List<User>> listUsers(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            q.like(User::getUsername, keyword).or().like(User::getPhone, keyword);
        }
        List<User> users = userMapper.selectList(q);
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @PutMapping("/users/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
        return Result.success();
    }

    @PutMapping("/users/{userId}/password")
    public Result<Void> resetPassword(@PathVariable Long userId, @RequestParam String password) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(encoder.encode(password));
            userMapper.updateById(user);
        }
        return Result.success();
    }

    // ==================== 群聊管理 ====================
    @GetMapping("/groups")
    public Result<List<GroupInfo>> listGroups(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<GroupInfo> q = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            q.like(GroupInfo::getName, keyword);
        }
        List<GroupInfo> groups = groupInfoMapper.selectList(q);
        return Result.success(groups);
    }

    @GetMapping("/groups/{groupId}/members")
    public Result<Map<String, Object>> getGroupMemberCount(@PathVariable Long groupId) {
        long count = groupMemberMapper.selectCount(
                new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId)
        );
        return Result.success(Map.of("count", count));
    }

    @DeleteMapping("/groups/{groupId}")
    public Result<Void> dissolveGroup(@PathVariable Long groupId) {
        groupMemberMapper.delete(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId));
        groupInfoMapper.deleteById(groupId);
        return Result.success();
    }

    // ==================== 机器人管理 ====================
    @GetMapping("/robots")
    public Result<List<Robot>> listAllRobots(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Robot> q = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            q.like(Robot::getName, keyword);
        }
        return Result.success(robotMapper.selectList(q));
    }

    // ==================== 敏感词管理 ====================
    @GetMapping("/sensitive-words")
    public Result<List<SensitiveWord>> listSensitiveWords() {
        return Result.success(sensitiveWordMapper.selectList(new LambdaQueryWrapper<SensitiveWord>()));
    }

    @PostMapping("/sensitive-word")
    public Result<Void> addSensitiveWord(@RequestBody SensitiveWord word) {
        sensitiveWordMapper.insert(word);
        return Result.success();
    }

    @DeleteMapping("/sensitive-word/{id}")
    public Result<Void> deleteSensitiveWord(@PathVariable Long id) {
        sensitiveWordMapper.deleteById(id);
        return Result.success();
    }

    // ==================== AI配置管理 ====================
    @GetMapping("/ai-config")
    public Result<List<AiConfig>> listAiConfigs(@RequestParam(required = false) Long robotId) {
        LambdaQueryWrapper<AiConfig> q = new LambdaQueryWrapper<>();
        if (robotId != null) q.eq(AiConfig::getRobotId, robotId);
        return Result.success(aiConfigMapper.selectList(q));
    }

    @PostMapping("/ai-config")
    public Result<Void> saveAiConfig(@RequestBody AiConfig config) {
        if (config.getId() != null) {
            aiConfigMapper.updateById(config);
        } else {
            aiConfigMapper.insert(config);
        }
        return Result.success();
    }

    @DeleteMapping("/ai-config/{id}")
    public Result<Void> deleteAiConfig(@PathVariable Long id) {
        aiConfigMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 系统配置管理 ====================
    @GetMapping("/system-configs")
    public Result<List<SystemConfig>> listSystemConfigs() {
        return Result.success(systemConfigMapper.selectList(null));
    }

    @PutMapping("/system-config/{key}")
    public Result<Void> updateSystemConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key)
        );
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(body.get("value"));
            systemConfigMapper.insert(config);
        } else {
            config.setConfigValue(body.get("value"));
            systemConfigMapper.updateById(config);
        }
        return Result.success();
    }

    // ==================== 群消息查看 ====================
    @GetMapping("/group-messages/{groupId}")
    public Result<List<Message>> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) Long beforeId) {
        // 查找群会话
        Conversation conv = conversationMapper.selectOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getType, "GROUP")
                        .eq(Conversation::getTypeId, groupId)
        );
        if (conv == null) return Result.success(List.of());

        LambdaQueryWrapper<Message> q = new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conv.getId())
                .eq(Message::getRecall, 0)
                .orderByDesc(Message::getId);
        if (beforeId != null) q.lt(Message::getId, beforeId);
        q.last("LIMIT " + limit);
        List<Message> msgs = messageMapper.selectList(q);
        return Result.success(msgs);
    }
}

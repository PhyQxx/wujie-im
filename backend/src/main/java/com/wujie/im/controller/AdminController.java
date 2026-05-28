package com.wujie.im.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.common.Result;
import com.wujie.im.entity.*;
import com.wujie.im.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
    @Autowired
    private org.redisson.api.RedissonClient redissonClient;
    
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

        // 实时指标
        stats.put("onlineUsers", com.wujie.im.websocket.WsHandler.getOnlineUserCount());
        
        String minuteKey = "stats:requests:minute:" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(java.time.LocalDateTime.now());
        long rpm = redissonClient.getAtomicLong(minuteKey).get();
        stats.put("requestsPerMinute", rpm);

        // 深层监控指标
        // JVM 内存
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        stats.put("jvmHeapUsed", heapUsage.getUsed() / 1024 / 1024); // MB
        stats.put("jvmHeapMax", heapUsage.getMax() / 1024 / 1024); // MB
        
            // Redis 状态 (尝试获取基本信息)
            try {
                org.redisson.api.NodesGroup<org.redisson.api.Node> nodes = redissonClient.getNodesGroup();
                int connectedClients = 0;
                long hits = 0;
                long misses = 0;
                for (org.redisson.api.Node node : nodes.getNodes()) {
                    Map<String, String> clientsInfo = node.info(org.redisson.api.Node.InfoSection.CLIENTS);
                    connectedClients += Integer.parseInt(clientsInfo.getOrDefault("connected_clients", "0"));
                    
                    Map<String, String> statsInfo = node.info(org.redisson.api.Node.InfoSection.STATS);
                    hits += Long.parseLong(statsInfo.getOrDefault("keyspace_hits", "0"));
                    misses += Long.parseLong(statsInfo.getOrDefault("keyspace_misses", "0"));
                }
                stats.put("redisClients", connectedClients);
                double hitRate = (hits + misses) == 0 ? 0 : (double) hits / (hits + misses) * 100;
                stats.put("redisHitRate", String.format("%.2f", hitRate));
                
                // 运行时长 (JVM)
                stats.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000); // seconds
            } catch (Exception e) {
                log.warn("Failed to get deep stats", e);
            }

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
        List<Robot> robots = robotMapper.selectList(q);
        for (Robot r : robots) {
            if (r.getOwnerId() != null) {
                User owner = userMapper.selectById(r.getOwnerId());
                if (owner != null) {
                    r.setOwnerName(owner.getUsername());
                }
            }
        }
        return Result.success(robots);
    }

    @PostMapping("/robot/{robotId}/join-group/{groupId}")
    public Result<Void> robotJoinGroup(@PathVariable Long robotId, @PathVariable Long groupId) {
        Robot robot = robotMapper.selectById(robotId);
        if (robot == null || robot.getVirtualUserId() == null) return Result.error("机器人不存在");
        GroupMember existing = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, robot.getVirtualUserId())
        );
        if (existing != null) return Result.error("机器人已在该群中");
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(robot.getVirtualUserId());
        member.setRole("MEMBER");
        member.setMuted(0);
        groupMemberMapper.insert(member);
        return Result.success();
    }

    @DeleteMapping("/robot/{robotId}/leave-group/{groupId}")
    public Result<Void> robotLeaveGroup(@PathVariable Long robotId, @PathVariable Long groupId) {
        Robot robot = robotMapper.selectById(robotId);
        if (robot == null || robot.getVirtualUserId() == null) return Result.error("机器人不存在");
        groupMemberMapper.delete(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, robot.getVirtualUserId())
        );
        return Result.success();
    }

    @GetMapping("/robot/{robotId}/groups")
    public Result<List<GroupInfo>> robotGroups(@PathVariable Long robotId) {
        Robot robot = robotMapper.selectById(robotId);
        if (robot == null || robot.getVirtualUserId() == null) return Result.success(List.of());
        List<GroupMember> memberships = groupMemberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getUserId, robot.getVirtualUserId())
        );
        if (memberships.isEmpty()) return Result.success(List.of());
        List<Long> groupIds = memberships.stream().map(GroupMember::getGroupId).toList();
        return Result.success(groupInfoMapper.selectBatchIds(groupIds));
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
    public Result<List<AiConfig>> listAiConfigs() {
        return Result.success(aiConfigMapper.selectList(new LambdaQueryWrapper<>()));
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

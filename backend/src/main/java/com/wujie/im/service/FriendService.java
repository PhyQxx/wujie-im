package com.wujie.im.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.FriendRelation;
import com.wujie.im.entity.FriendRequest;
import com.wujie.im.entity.Notification;
import com.wujie.im.entity.User;
import com.wujie.im.mapper.FriendRelationMapper;
import com.wujie.im.mapper.FriendRequestMapper;
import com.wujie.im.mapper.UserMapper;
import com.wujie.im.service.NotificationService;
import com.wujie.im.websocket.WsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FriendService {
    @Autowired
    private FriendRequestMapper friendRequestMapper;
    @Autowired
    private FriendRelationMapper friendRelationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private WsHandler wsHandler;

    public void sendRequest(Long fromUserId, Long toUserId, String reason) {
        FriendRelation exist = friendRelationMapper.selectOne(
                new LambdaQueryWrapper<FriendRelation>()
                        .eq(FriendRelation::getUserId, fromUserId)
                        .eq(FriendRelation::getFriendId, toUserId)
        );
        if (exist != null) {
            throw new RuntimeException("你们已经是好友了");
        }
        FriendRequest req = new FriendRequest();
        req.setFromUserId(fromUserId);
        req.setToUserId(toUserId);
        req.setReason(reason);
        req.setStatus("PENDING");
        friendRequestMapper.insert(req);

        // 通过WebSocket通知被申请者
        notificationService.sendNotification(toUserId, "FRIEND_REQUEST",
                "新的好友申请", "用户 " + fromUserId + " 申请加你为好友", req.getId());

        // WebSocket实时推送
        Notification notif = new Notification();
        notif.setUserId(toUserId);
        notif.setType("FRIEND_REQUEST");
        notif.setTitle("新的好友申请");
        notif.setContent("用户 " + fromUserId + " 申请加你为好友");
        notif.setSourceId(req.getId());
        Map<String, Object> pushData = new HashMap<>();
        pushData.put("type", "notification");
        pushData.put("data", notif);
        wsHandler.sendToUser(toUserId, JSONUtil.toJsonStr(pushData));
    }

    public List<FriendRequest> getRequests(Long userId) {
        return friendRequestMapper.selectList(
                new LambdaQueryWrapper<FriendRequest>()
                        .eq(FriendRequest::getToUserId, userId)
                        .orderByDesc(FriendRequest::getCreateTime)
        );
    }

    public void handleRequest(Long requestId, String action) {
        FriendRequest req = friendRequestMapper.selectById(requestId);
        if (req == null) throw new RuntimeException("申请不存在");
        if ("AGREE".equals(action)) {
            req.setStatus("AGREED");
            friendRequestMapper.updateById(req);
            FriendRelation r1 = new FriendRelation();
            r1.setUserId(req.getFromUserId());
            r1.setFriendId(req.getToUserId());
            r1.setGroupId(1L); // 默认分组
            friendRelationMapper.insert(r1);
            FriendRelation r2 = new FriendRelation();
            r2.setUserId(req.getToUserId());
            r2.setFriendId(req.getFromUserId());
            r2.setGroupId(1L);
            friendRelationMapper.insert(r2);
        } else if ("REJECT".equals(action)) {
            req.setStatus("REJECTED");
            friendRequestMapper.updateById(req);
        }
    }

    public List<Map<String, Object>> getFriends(Long userId) {
        List<FriendRelation> relations = friendRelationMapper.selectList(
                new LambdaQueryWrapper<FriendRelation>().eq(FriendRelation::getUserId, userId)
        );
        return relations.stream().map(r -> {
            User u = userMapper.selectById(r.getFriendId());
            if (u != null) u.setPassword(null);
            Map<String, Object> map = new HashMap<>();
            map.put("user", u);
            map.put("groupId", r.getGroupId());
            map.put("remark", r.getRemark());
            return map;
        }).toList();
    }

    public void deleteFriend(Long userId, Long friendId) {
        friendRelationMapper.delete(
                new LambdaQueryWrapper<FriendRelation>()
                        .eq(FriendRelation::getUserId, userId)
                        .eq(FriendRelation::getFriendId, friendId)
        );
        friendRelationMapper.delete(
                new LambdaQueryWrapper<FriendRelation>()
                        .eq(FriendRelation::getUserId, friendId)
                        .eq(FriendRelation::getFriendId, userId)
        );
    }

    public void moveFriendToGroup(Long userId, Long friendId, Long groupId) {
        FriendRelation relation = friendRelationMapper.selectOne(
                new LambdaQueryWrapper<FriendRelation>()
                        .eq(FriendRelation::getUserId, userId)
                        .eq(FriendRelation::getFriendId, friendId)
        );
        if (relation != null) {
            relation.setGroupId(groupId);
            friendRelationMapper.updateById(relation);
        }
    }

    public void setFriendRemark(Long userId, Long friendId, String remark) {
        FriendRelation relation = friendRelationMapper.selectOne(
                new LambdaQueryWrapper<FriendRelation>()
                        .eq(FriendRelation::getUserId, userId)
                        .eq(FriendRelation::getFriendId, friendId)
        );
        if (relation != null) {
            relation.setRemark(remark);
            friendRelationMapper.updateById(relation);
        }
    }
}

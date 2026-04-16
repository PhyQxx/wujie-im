package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.FriendRelation;
import com.wujie.im.entity.FriendRequest;
import com.wujie.im.entity.User;
import com.wujie.im.mapper.FriendRelationMapper;
import com.wujie.im.mapper.FriendRequestMapper;
import com.wujie.im.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class FriendService {
    @Autowired
    private FriendRequestMapper friendRequestMapper;
    @Autowired
    private FriendRelationMapper friendRelationMapper;
    @Autowired
    private UserMapper userMapper;

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
            friendRelationMapper.insert(r1);
            FriendRelation r2 = new FriendRelation();
            r2.setUserId(req.getToUserId());
            r2.setFriendId(req.getFromUserId());
            friendRelationMapper.insert(r2);
        } else if ("REJECT".equals(action)) {
            req.setStatus("REJECTED");
            friendRequestMapper.updateById(req);
        }
    }

    public List<User> getFriends(Long userId) {
        List<FriendRelation> relations = friendRelationMapper.selectList(
                new LambdaQueryWrapper<FriendRelation>().eq(FriendRelation::getUserId, userId)
        );
        return relations.stream().map(r -> {
            User u = userMapper.selectById(r.getFriendId());
            if (u != null) u.setPassword(null);
            return u;
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
}

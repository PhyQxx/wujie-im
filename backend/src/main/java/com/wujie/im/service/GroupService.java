package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.GroupInfo;
import com.wujie.im.entity.GroupMember;
import com.wujie.im.entity.GroupJoinRequest;
import com.wujie.im.mapper.GroupInfoMapper;
import com.wujie.im.mapper.GroupMemberMapper;
import com.wujie.im.mapper.GroupJoinRequestMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class GroupService {
    @Autowired
    private GroupInfoMapper groupInfoMapper;
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    @Autowired
    private GroupJoinRequestMapper groupJoinRequestMapper;

    public GroupInfo createGroup(String name, String avatar, String type, Long ownerId) {
        GroupInfo group = new GroupInfo();
        group.setName(name);
        group.setAvatar(avatar);
        group.setType(type != null ? type : "PUBLIC");
        group.setOwnerId(ownerId);
        groupInfoMapper.insert(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(ownerId);
        member.setRole("OWNER");
        groupMemberMapper.insert(member);
        return group;
    }

    public List<GroupInfo> getUserGroups(Long userId) {
        List<GroupMember> memberships = groupMemberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getUserId, userId)
        );
        return memberships.stream().map(m -> groupInfoMapper.selectById(m.getGroupId())).toList();
    }

    public List<GroupMember> getGroupMembers(Long groupId) {
        return groupMemberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId)
        );
    }

    public void joinGroup(Long groupId, Long userId, String reason) {
        GroupJoinRequest req = new GroupJoinRequest();
        req.setGroupId(groupId);
        req.setUserId(userId);
        req.setReason(reason);
        req.setStatus("PENDING");
        groupJoinRequestMapper.insert(req);
    }

    public void handleJoinRequest(Long requestId, Long reviewerId, String action) {
        GroupJoinRequest req = groupJoinRequestMapper.selectById(requestId);
        if (req == null) throw new RuntimeException("申请不存在");
        if ("AGREE".equals(action)) {
            req.setStatus("AGREED");
            req.setReviewerId(reviewerId);
            groupJoinRequestMapper.updateById(req);
            GroupMember member = new GroupMember();
            member.setGroupId(req.getGroupId());
            member.setUserId(req.getUserId());
            member.setRole("MEMBER");
            groupMemberMapper.insert(member);
        } else if ("REJECT".equals(action)) {
            req.setStatus("REJECTED");
            req.setReviewerId(reviewerId);
            groupJoinRequestMapper.updateById(req);
        }
    }
}

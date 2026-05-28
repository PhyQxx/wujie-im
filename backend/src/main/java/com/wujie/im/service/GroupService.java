package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.*;
import com.wujie.im.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupService {
    @Autowired
    private GroupInfoMapper groupInfoMapper;
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    @Autowired
    private GroupJoinRequestMapper groupJoinRequestMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private org.redisson.api.RedissonClient redissonClient;

    private static final String GROUP_INVITE_PREFIX = "group_invite:";

    public String generateInviteToken(Long groupId, Long operatorId) {
        GroupMember member = getMemberRole(groupId, operatorId);
        if (member == null) throw new RuntimeException("非群成员无法生成邀请链接");
        
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        redissonClient.getBucket(GROUP_INVITE_PREFIX + token).set(groupId, 7, java.util.concurrent.TimeUnit.DAYS);
        return token;
    }

    @Transactional
    public void joinByInviteToken(String token, Long userId) {
        Object groupIdObj = redissonClient.getBucket(GROUP_INVITE_PREFIX + token).get();
        if (groupIdObj == null) throw new RuntimeException("邀请链接已失效");
        
        Long groupId = Long.valueOf(groupIdObj.toString());
        // 检查是否已在群中
        GroupMember existing = getMemberRole(groupId, userId);
        if (existing != null) return;

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        groupMemberMapper.insert(member);
        
        log.info("用户 {} 通过邀请链接加入群组 {}", userId, groupId);
    }

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

    public GroupInfo getGroupById(Long groupId) {
        return groupInfoMapper.selectById(groupId);
    }

    public List<GroupMember> getGroupMembers(Long groupId) {
        return groupMemberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId)
        );
    }

    public List<Long> getGroupMemberIds(Long groupId) {
        List<GroupMember> members = groupMemberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId)
        );
        return members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
    }

    public GroupMember getMemberRole(Long groupId, Long userId) {
        return groupMemberMapper.selectOne(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userId)
        );
    }

    public void joinGroup(Long groupId, Long userId, String reason) {
        // 检查是否已在群中
        GroupMember existing = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userId)
        );
        if (existing != null) return;

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

    public List<GroupJoinRequest> getJoinRequests(Long groupId) {
        return groupJoinRequestMapper.selectList(
                new LambdaQueryWrapper<GroupJoinRequest>()
                        .eq(GroupJoinRequest::getGroupId, groupId)
                        .orderByDesc(GroupJoinRequest::getCreateTime)
        );
    }

    public List<GroupJoinRequest> getUserJoinRequests(Long userId) {
        return groupJoinRequestMapper.selectList(
                new LambdaQueryWrapper<GroupJoinRequest>()
                        .eq(GroupJoinRequest::getUserId, userId)
                        .orderByDesc(GroupJoinRequest::getCreateTime)
        );
    }

    @Transactional
    public void updateGroup(Long groupId, String name, String avatar, String announcement, Long operatorId) {
        GroupInfo group = groupInfoMapper.selectById(groupId);
        if (group == null) throw new RuntimeException("群不存在");
        GroupMember member = getMemberRole(groupId, operatorId);
        if (member == null || (!"OWNER".equals(member.getRole()) && !"ADMIN".equals(member.getRole()))) {
            throw new RuntimeException("无权限操作");
        }
        if (name != null) group.setName(name);
        if (avatar != null) group.setAvatar(avatar);
        if (announcement != null) group.setAnnouncement(announcement);
        groupInfoMapper.updateById(group);
    }

    @Transactional
    public void muteMember(Long groupId, Long targetUserId, Integer minutes, Long operatorId) {
        GroupMember operator = getMemberRole(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            throw new RuntimeException("无权限操作");
        }
        GroupMember target = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, targetUserId)
        );
        if (target == null) throw new RuntimeException("成员不在群中");
        target.setMuted(1);
        if (minutes != null && minutes > 0) {
            target.setMutedUntil(LocalDateTime.now().plusMinutes(minutes));
        } else {
            target.setMutedUntil(null);
        }
        groupMemberMapper.updateById(target);
    }

    public void unmuteMember(Long groupId, Long targetUserId, Long operatorId) {
        GroupMember operator = getMemberRole(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            throw new RuntimeException("无权限操作");
        }
        GroupMember target = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, targetUserId)
        );
        if (target == null) throw new RuntimeException("成员不在群中");
        target.setMuted(0);
        target.setMutedUntil(null);
        groupMemberMapper.updateById(target);
    }

    @Transactional
    public void inviteMembers(Long groupId, List<Long> userIds, Long inviterId) {
        GroupMember inviter = getMemberRole(groupId, inviterId);
        if (inviter == null) throw new RuntimeException("无权限操作");
        for (Long userId : userIds) {
            GroupMember existing = groupMemberMapper.selectOne(
                    new LambdaQueryWrapper<GroupMember>()
                            .eq(GroupMember::getGroupId, groupId)
                            .eq(GroupMember::getUserId, userId)
            );
            if (existing == null) {
                GroupMember member = new GroupMember();
                member.setGroupId(groupId);
                member.setUserId(userId);
                member.setRole("MEMBER");
                groupMemberMapper.insert(member);
            }
        }
    }

    @Transactional
    public void removeMember(Long groupId, Long targetUserId, Long operatorId) {
        GroupMember operator = getMemberRole(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            throw new RuntimeException("无权限操作");
        }
        groupMemberMapper.delete(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, targetUserId)
        );
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupMember member = getMemberRole(groupId, userId);
        if (member == null) return;
        if ("OWNER".equals(member.getRole())) {
            throw new RuntimeException("群主无法退出，请先转让群主");
        }
        groupMemberMapper.delete(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userId)
        );
    }

    @Transactional
    public void setAdmin(Long groupId, Long targetUserId, Long operatorId, boolean isAdmin) {
        GroupMember operator = getMemberRole(groupId, operatorId);
        if (operator == null || !"OWNER".equals(operator.getRole())) {
            throw new RuntimeException("只有群主可以设置管理员");
        }
        GroupMember target = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, targetUserId)
        );
        if (target == null) throw new RuntimeException("成员不在群中");
        target.setRole(isAdmin ? "ADMIN" : "MEMBER");
        groupMemberMapper.updateById(target);
    }

    public void dissolveGroup(Long groupId, Long userId) {
        GroupInfo group = groupInfoMapper.selectById(groupId);
        if (group == null) throw new RuntimeException("群不存在");
        if (!group.getOwnerId().equals(userId)) throw new RuntimeException("只有群主可以解散群");
        groupMemberMapper.delete(new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId));
        groupJoinRequestMapper.delete(new LambdaQueryWrapper<GroupJoinRequest>().eq(GroupJoinRequest::getGroupId, groupId));
        groupInfoMapper.deleteById(groupId);
    }

    @Transactional
    public void muteGroup(Long groupId, boolean mute, Long operatorId) {
        GroupMember operator = getMemberRole(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            throw new RuntimeException("无权限操作");
        }
        GroupInfo group = groupInfoMapper.selectById(groupId);
        group.setMuteAll(mute ? 1 : 0);
        groupInfoMapper.updateById(group);
    }

    @Transactional
    public void pinMessage(Long groupId, Long messageId, Long operatorId) {
        GroupMember operator = getMemberRole(groupId, operatorId);
        if (operator == null || (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole()))) {
            throw new RuntimeException("无权限操作");
        }
        GroupInfo group = groupInfoMapper.selectById(groupId);
        group.setPinnedMessageId(messageId);
        groupInfoMapper.updateById(group);
    }

    @Transactional
    public void transferOwnership(Long groupId, Long newOwnerId, Long operatorId) {
        GroupInfo group = groupInfoMapper.selectById(groupId);
        if (!group.getOwnerId().equals(operatorId)) {
            throw new RuntimeException("只有原群主可以转让群");
        }
        
        GroupMember oldOwner = getMemberRole(groupId, operatorId);
        GroupMember newOwner = getMemberRole(groupId, newOwnerId);
        if (newOwner == null) throw new RuntimeException("新群主不在群中");

        oldOwner.setRole("MEMBER");
        newOwner.setRole("OWNER");
        groupMemberMapper.updateById(oldOwner);
        groupMemberMapper.updateById(newOwner);

        group.setOwnerId(newOwnerId);
        groupInfoMapper.updateById(group);
    }

    public boolean isMuted(Long groupId, Long userId) {
        GroupInfo group = groupInfoMapper.selectById(groupId);
        GroupMember member = getMemberRole(groupId, userId);
        if (member == null) return false;

        // 全体禁言检查（管理员和群主豁免）
        if (group.getMuteAll() != null && group.getMuteAll() == 1) {
            if (!"OWNER".equals(member.getRole()) && !"ADMIN".equals(member.getRole())) {
                return true;
            }
        }

        // 个人禁言检查
        if (member.getMuted() != null && member.getMuted() == 1) {
            if (member.getMutedUntil() != null && member.getMutedUntil().isBefore(LocalDateTime.now())) {
                member.setMuted(0);
                member.setMutedUntil(null);
                groupMemberMapper.updateById(member);
                return false;
            }
            return true;
        }
        return false;
    }
}

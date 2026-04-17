package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.FriendGroup;
import com.wujie.im.mapper.FriendGroupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendGroupService {
    private final FriendGroupMapper friendGroupMapper;

    public List<FriendGroup> getUserGroups(Long userId) {
        return friendGroupMapper.selectList(
                new LambdaQueryWrapper<FriendGroup>()
                        .eq(FriendGroup::getUserId, userId)
                        .orderByAsc(FriendGroup::getSort)
        );
    }

    public FriendGroup createGroup(Long userId, String name) {
        FriendGroup group = new FriendGroup();
        group.setUserId(userId);
        group.setName(name);
        friendGroupMapper.insert(group);
        return group;
    }

    public void updateGroup(Long groupId, String name) {
        FriendGroup group = friendGroupMapper.selectById(groupId);
        if (group != null && name != null) {
            group.setName(name);
            friendGroupMapper.updateById(group);
        }
    }

    public void deleteGroup(Long groupId) {
        friendGroupMapper.deleteById(groupId);
    }

    public void moveFriendToGroup(Long userId, Long friendId, Long groupId) {
        // 已在 FriendService 中处理
    }
}

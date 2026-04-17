package com.wujie.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wujie.im.entity.User;
import com.wujie.im.entity.UserProfile;
import com.wujie.im.mapper.UserMapper;
import com.wujie.im.mapper.UserProfileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserProfileMapper userProfileMapper;

    public List<User> listUsers(String keyword) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            q.like(User::getUsername, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getEmail, keyword);
        }
        q.eq(User::getStatus, 1);
        List<User> users = userMapper.selectList(q);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
            // 合并 UserProfile 数据
            UserProfile profile = userProfileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, id)
            );
            if (profile != null) {
                user.setNickname(profile.getNickname());
                user.setAvatar(profile.getAvatar());
                user.setSignature(profile.getSignature());
            }
        }
        return user;
    }

    public void updateStatus(Long userId, String status) {
        User user = new User();
        user.setId(userId);
        user.setUserStatus(status);
        userMapper.updateById(user);
    }

    public UserProfile getProfile(Long userId) {
        return userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );
    }

    public void updateProfile(UserProfile profile) {
        UserProfile existing = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, profile.getUserId())
        );
        if (existing != null) {
            profile.setId(existing.getId());
            userProfileMapper.updateById(profile);
        } else {
            userProfileMapper.insert(profile);
        }
    }
}

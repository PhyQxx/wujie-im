package com.wujie.im.controller;

import com.wujie.im.common.Encrypt;
import com.wujie.im.common.Result;
import com.wujie.im.entity.FriendGroup;
import com.wujie.im.service.FriendGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend-group")
@RequiredArgsConstructor
public class FriendGroupController {
    private final FriendGroupService friendGroupService;

    @Encrypt
    @GetMapping("/list/{userId}")
    public Result<List<FriendGroup>> getGroups(@PathVariable Long userId) {
        return Result.success(friendGroupService.getUserGroups(userId));
    }

    @Encrypt
    @PostMapping("/create")
    public Result<FriendGroup> createGroup(@RequestBody Map<String, Object> params) {
        return Result.success(friendGroupService.createGroup(
                Long.valueOf(params.get("userId").toString()),
                (String) params.get("name")
        ));
    }

    @Encrypt
    @PutMapping("/{groupId}")
    public Result<Void> updateGroup(@PathVariable Long groupId, @RequestBody Map<String, String> params) {
        friendGroupService.updateGroup(groupId, params.get("name"));
        return Result.success();
    }

    @Encrypt
    @DeleteMapping("/{groupId}")
    public Result<Void> deleteGroup(@PathVariable Long groupId) {
        friendGroupService.deleteGroup(groupId);
        return Result.success();
    }
}

# 無界（wujie-im）接口验收文档

> 创建时间：2026-04-16
> 版本：v1.0
> 测试负责人：QA

---

## 一、验收环境

| 项目 | 信息 |
|------|------|
| 后端地址 | http://localhost:8092 |
| 数据库 | MySQL wujie_im（172.20.0.2:3306） |
| WebSocket | ws://localhost:8092/ws |
| 前端地址 | http://localhost:3000 |
| GitHub | https://github.com/PhyQxx/wujie-im |

**启动后端命令：**
```bash
cd /root/.openclaw/workspace-dev/wujie-im/backend
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 mvn spring-boot:run
```

**启动前端命令：**
```bash
cd /root/.openclaw/workspace-dev/wujie-im/frontend && npm run dev
```

---

## 二、接口验收清单（P0）

### 1. 认证模块

| # | 接口 | 方法 | 路径 | 验收标准 | 状态 |
|---|------|------|------|----------|------|
| 1.1 | 注册 | POST | /api/auth/register | username+password必填，返回userId | □ |
| 1.2 | 登录 | POST | /api/auth/login | 返回accessToken/refreshToken/userId | □ |
| 1.3 | 刷新Token | POST | /api/auth/refresh | 用refreshToken换新accessToken | □ |

### 2. 用户模块

| # | 接口 | 方法 | 路径 | 验收标准 | 状态 |
|---|------|------|------|----------|------|
| 2.1 | 用户搜索 | GET | /api/user/list?keyword= | 返回匹配用户列表，密码脱敏 | □ |
| 2.2 | 用户详情 | GET | /api/user/{id} | 返回用户信息 | □ |
| 2.3 | 更新状态 | PUT | /api/user/status | 更新ONLINE/OFFLINE/AWAY/DND | □ |

### 3. 好友模块

| # | 接口 | 方法 | 路径 | 验收标准 | 状态 |
|---|------|------|------|----------|------|
| 3.1 | 发送申请 | POST | /api/friend/request | fromUserId+toUserId+reason | □ |
| 3.2 | 申请列表 | GET | /api/friend/requests/{userId} | 返回申请列表，带申请人信息 | □ |
| 3.3 | 处理申请 | PUT | /api/friend/request/{id}?action=AGREE\|REJECT | AGREE双向加好友 | □ |
| 3.4 | 好友列表 | GET | /api/friend/list/{userId} | 返回好友列表 | □ |
| 3.5 | 删除好友 | DELETE | /api/friend/{userId}/{friendId} | 双向删除 | □ |

### 4. 会话模块

| # | 接口 | 方法 | 路径 | 验收标准 | 状态 |
|---|------|------|------|----------|------|
| 4.1 | 会话列表 | GET | /api/conversation/list/{userId} | 按最后消息时间倒序，含targetUser/groupInfo | □ |
| 4.2 | 消息历史 | GET | /api/conversation/{id}/messages?beforeId=&limit= | 分页返回，含发送者信息 | □ |

### 5. 消息模块

| # | 接口 | 方法 | 路径 | 验收标准 | 状态 |
|---|------|------|------|----------|------|
| 5.1 | 标记已读 | PUT | /api/message/read | userId+conversationId+messageId | □ |
| 5.2 | 撤回消息 | PUT | /api/message/recall/{messageId}?userId= | 仅发送者可撤回 | □ |

### 6. 群聊模块

| # | 接口 | 方法 | 路径 | 验收标准 | 状态 |
|---|------|------|------|----------|------|
| 6.1 | 创建群 | POST | /api/group | name+type+ownerId | □ |
| 6.2 | 群详情 | GET | /api/group/{groupId} | 返回群信息 | □ |
| 6.3 | 群成员 | GET | /api/group/{groupId}/members | 返回成员列表含角色 | □ |
| 6.4 | 申请入群 | POST | /api/group/{groupId}/join | userId+reason | □ |
| 6.5 | 审批入群 | PUT | /api/group/join-request/{id}?action= | AGREE加入群 | □ |

### 7. WebSocket 验收

| # | 场景 | 操作 | 预期结果 | 状态 |
|---|------|------|----------|------|
| 7.1 | 连接认证 | 连接/ws，发送`{"type":"auth","token":"..."}` | 返回认证成功 | □ |
| 7.2 | 心跳 | 30秒无操作后发送`{"type":"heartbeat"}` | 收到pong响应 | □ |
| 7.3 | 发送消息 | 发送`{"type":"send_message",...}` | 收到确认消息推送 | □ |
| 7.4 | 接收消息 | 对方发送消息 | 实时收到消息推送 | □ |
| 7.5 | 撤回消息 | 发送`{"type":"recall_message","userId":,"messageId":}` | 收到撤回通知 | □ |
| 7.6 | 好友申请通知 | 有人申请加好友 | 实时收到通知推送 | □ |

---

## 三、验收通过标准

1. 所有接口返回格式统一：`{ "code": 200, "msg": "success", "data": ... }`
2. JWT Token 鉴权正常（401返回）
3. 数据库读写正常（注册后能登录，登录后可获取用户信息）
4. WebSocket 消息收发正常（发送→接收完整）
5. 错误情况有明确错误信息返回

---

## 四、测试账号准备

```bash
# 注册测试账号
curl -X POST http://localhost:8092/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test1","password":"123456"}'

curl -X POST http://localhost:8092/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test2","password":"123456"}'

# 登录获取token
curl -X POST http://localhost:8092/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test1","password":"123456"}'
```

---

## 五、发现的问题记录

| 日期 | 模块 | 问题描述 | 严重程度 | 状态 |
|------|------|----------|----------|------|
| - | - | - | - | - |

---

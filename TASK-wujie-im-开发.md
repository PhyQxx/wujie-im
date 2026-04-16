# TASK-wujie-im-开发.md

## 任务派单

**派单时间：** 2026-04-16
**派单人：** PD（项目总监）
**接收人：** Dev（开发工程师）
**项目：** 无界（wujie-im）即时通讯系统
**项目仓库名：** wujie-im

---

## 项目概述

- **产品名：** 无界（wujie-im）
- **产品定位：** PC Web 即时通讯平台（飞书+Discord+Slack风格）
- **技术栈：** Vue3 + SpringBoot + MySQL + Redis + WebSocket
- **目标用户：** 内部团队协作 + 社区论坛用户
- **AI能力：** MiniMax / GLM（智谱）/ DeepSeek 多模型接入

---

## 需求文档

PRD 完整路径：
`/vol2/1000/我的文档/obsidian/PHY/项目/08.IM即时通讯/PRD-即时通讯系统.md`

---

## 设计稿路径

`/root/.openclaw/workspace-des/prototype/即时通讯系统（IM）/`

包含：
- `DESIGN-SPEC-IM.md` — 设计规范
- `im-login.html` — 登录页
- `im-register.html` — 注册页
- `im-main.html` — 主聊天页
- `im-contacts.html` — 联系人页
- `im-group-detail.html` — 群聊详情页
- `im-bot-config.html` — 机器人配置页
- `im-my-bots.html` — 我的机器人
- `im-admin.html` — 管理员后台
- `im-settings.html` — 个人设置

---

## 核心功能模块（P0）

1. **用户体系** - 注册/登录/会话管理/JWT认证
2. **好友系统** - 添加好友/申请/同意/拒绝/列表/黑名单
3. **单聊** - 私聊消息/消息漫游/已读未读/消息撤回
4. **群聊** - 创建群/申请加入/群成员管理/群消息/群公告/禁言
5. **AI 机器人** - MiniMax/GLM/DeepSeek多模型接入/@艾特回复/上下文记忆
6. **自定义机器人** - 关键字回复/@回复/Webhook触发
7. **管理员后台** - 数据看板/用户管理/AI配置/机器人管理/系统配置
8. **通知系统** - 好友申请/入群申请/系统通知
9. **消息系统** - WebSocket实时推送/消息加密/历史记录

---

## 消息状态规范

| 状态 | 视觉 |
|------|------|
| 发送中 | 转圈动画 |
| 已发送 | 单勾 ✓ |
| 已送达 | 双勾 ✓✓ |
| 已读 | 双勾变蓝 ✓✓ |

---

## 在线状态规范

| 状态 | 颜色 |
|------|------|
| 在线 | #10B981 |
| 离开 | #F59E0B |
| 勿扰 | #EF4444 |
| 离线 | #9CA3AF |

---

## WebSocket 协议

| 消息方向 | 类型 | 说明 |
|----------|------|------|
| C→S | auth | 认证（token） |
| C→S | heartbeat | 心跳（30s一次） |
| C→S | send_message | 发送消息 |
| C→S | read_message | 标记已读 |
| C→S | recall_message | 撤回消息 |
| S→C | message | 新消息推送 |
| S→C | notification | 通知 |
| S→C | sync | 同步消息 |

---

## 消息加密方案

- HTTPS（WSS）全链路 TLS 加密
- 请求体：AES-256-GCM 加密
- 密钥交换：RSA-2048
- JWT Access Token（15min）+ Refresh Token（7d）

---

## 数据库核心表

- user / user_profile
- friend_request / friend_relation
- conversation / message / message_read
- group_info / group_member / group_join_request
- robot / robot_rule / ai_config
- notification / sensitive_word
- admin_user

---

## 交付要求

### 后端
1. SpringBoot 项目初始化
2. 数据库表结构设计（按PRD核心表）
3. 用户注册/登录接口（JWT）
4. 好友系统接口
5. 单聊接口 + WebSocket
6. 群聊接口
7. AI 机器人接口
8. 自定义机器人接口
9. 管理员后台接口
10. 通知系统

### 前端
1. Vue3 + Vite 项目初始化
2. 登录/注册页
3. 主聊天页（三栏布局）
4. 联系人页
5. 群聊详情页
6. 机器人配置页
7. 我的机器人页
8. 管理员后台页
9. 个人设置页

---

## 开发顺序建议

1. 后端项目初始化 + 数据库设计
2. 用户认证（注册/登录/JWT）
3. 好友系统
4. 单聊（WebSocket实时消息）
5. 群聊系统
6. AI机器人 + 自定义机器人
7. 管理员后台
8. 前端页面开发（与后端并行）

---

## 完成后

请通知我（PD）阶段性成果，提交代码仓库地址，我进行进度跟踪和验收。

# wujie-im

無界即时通讯系统 — 融合飞书、Discord、Slack 风格的 PC Web 即时通讯平台

## 技术栈

| 层级 | 技术选型 |
|------|----------|
| 前端 | Vue 3.4 + Vite 5 + TypeScript + Element Plus + Pinia |
| 后端 | SpringBoot 3.2.0 + MyBatis-Plus 3.5 |
| 数据库 | MySQL + Redis (Redisson) |
| 实时通信 | WebSocket |
| 认证授权 | JWT (Access Token 15分钟 + Refresh Token 7天) |
| AI 集成 | MiniMax / GLM / DeepSeek (策略模式) |

## 功能特性

### 用户系统
- 用户注册与登录（支持 JWT 双 Token 认证机制）
- 好友管理（添加、删除、好友列表）

### 即时通讯
- 实时 WebSocket 消息收发
- 消息撤回与已读状态
- 单聊与群聊支持

### 群聊功能
- 创建群聊、群成员管理
- 群聊消息实时推送

### AI 机器人
- 支持配置 MiniMax、GLM、DeepSeek 三种 AI 服务
- 策略模式实现多 AI 切换
- 与 AI 机器人对话

### 管理员后台
- 用户管理
- 系统监控

### 通知系统
- 实时通知推送
- 通知状态管理

## 环境要求

- Node.js 18+
- JDK 17+
- MySQL 8.0+
- Redis 6.0+

## 快速开始

### 前端启动

```bash
cd frontend
npm install          # 安装依赖
npm run dev          # 启动开发服务器 (http://localhost:3000)
npm run build        # 生产环境构建
npm run preview      # 预览生产构建
```

### 后端启动

```bash
cd backend
mvn clean package    # 构建 JAR 包
mvn spring-boot:run  # 直接运行
```

### 配置说明

**前端配置** (`frontend/vite.config.ts`):
- 开发代理: `/api` → `localhost:18092`
- WebSocket 代理: `/ws` → `ws://localhost:18092`

**后端配置** (`backend/src/main/resources/application.yml`):
- 服务端口: `18092`
- MySQL: `mysql.pnkx.top:13306`
- Redis: `127.0.0.1:6379`

## 项目结构

```
wujie-im/
├── frontend/src/
│   ├── views/          # 页面组件 (Login、Register、Main、Contacts、MyRobots、Settings、AdminDashboard、RobotConfig、GroupDetail)
│   ├── components/     # 可复用 UI 组件 (ChatWindow、Sidebar、ConversationList、MessageBubble 等)
│   ├── stores/         # Pinia 状态管理 (user、friend、conversation、message、group、notification、robot)
│   ├── api/            # API 模块 (auth、user、friend、message、group、robot、notification、admin)
│   ├── router/         # Vue Router 路由配置 (含登录守卫)
│   ├── types/          # TypeScript 类型定义
│   └── utils/          # 工具函数 (WebSocket、Axios 请求封装、Crypto 加密)
│
├── backend/src/main/java/com/wujie/im/
│   ├── controller/     # REST 接口控制器 (Auth、User、Friend、Message、Group、Robot、Notification、Admin)
│   ├── service/        # 业务逻辑层 (含 ai/ 子包实现 AI 集成)
│   ├── mapper/         # MyBatis-Plus 数据访问层
│   ├── entity/         # 数据库实体类
│   ├── dto/           # 数据传输对象
│   ├── websocket/      # WebSocket 处理器 (WsHandler)
│   ├── config/        # Spring 配置类 (WebSocket、CORS、Redis、MyBatis-Plus)
│   ├── common/         # 公共工具类 (JwtUtil 等)
│   └── enums/          # 枚举类型定义
```

## 核心设计

### WebSocket 通信协议

JSON 格式消息，通过 `type` 字段区分消息类型：

| type | 说明 |
|------|------|
| `auth` | 心跳认证 |
| `heartbeat` | 心跳保活 |
| `send_message` | 发送消息 |
| `read_message` | 标记已读 |
| `recall_message` | 撤回消息 |

### JWT 双 Token 认证

- Access Token：有效期 15 分钟，用于 API 接口访问
- Refresh Token：有效期 7 天，用于刷新 Access Token
- 前端存储于 localStorage

### AI 集成策略模式

```
AiService (接口)
├── MiniMaxService  (MiniMax AI 实现)
├── GlmService      (GLM AI 实现)
└── DeepSeekService (DeepSeek AI 实现)
```

## 许可证

MIT

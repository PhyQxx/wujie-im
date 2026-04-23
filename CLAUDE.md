# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**wujie-im** is a PC Web instant messaging platform (Feishu + Discord + Slack style) built with Vue3 + SpringBoot + MySQL + Redis + WebSocket. It supports AI robots (MiniMax/GLM/DeepSeek), friend systems, group chats, and admin dashboards.

## Build Commands

### Frontend (Vue3 + Vite)
```bash
cd frontend
npm install          # Install dependencies
npm run dev          # Start dev server on port 3000
npm run build        # Production build
npm run preview      # Preview production build
```

### Backend (SpringBoot 3.2.0 + Maven)
```bash
cd backend
export JAVA_HOME=/home/phy/.jdks/corretto-17.0.18  # Required: Java 17+ for Spring Boot 3.2 Maven plugin
mvn clean package    # Build JAR
mvn spring-boot:run  # Run directly
# Requires: MySQL (mysql.pnkx.top:13306), Redis (127.0.0.1:6379)
```

## Architecture

### Frontend (`frontend/src/`)
- `views/` - Page components (Login, Register, Main, Contacts, MyRobots, Settings, AdminDashboard, RobotConfig, GroupDetail)
- `components/` - Reusable UI components (ChatWindow, Sidebar, ConversationList, MessageBubble, etc.)
- `stores/` - Pinia stores (user, friend, conversation, message, group, notification, robot)
- `api/` - API modules (auth, user, friend, message, group, robot, notification, admin)
- `router/` - Vue Router with auth guard
- `types/` - TypeScript type definitions
- `utils/` - Utilities (websocket, request/axios, crypto)

### Backend (`backend/src/main/java/com/wujie/im/`)
- `controller/` - REST endpoints (Auth, User, Friend, Message, Group, Robot, Notification, Admin)
- `service/` - Business logic, includes `ai/` subpackage for AI integrations
- `mapper/` - MyBatis-Plus mappers
- `entity/` - Database entities
- `dto/` - Data transfer objects
- `websocket/` - WebSocket handler (WsHandler) for real-time messaging
- `config/` - Spring configs (WebSocket, CORS, Redis, MyBatis-Plus)
- `common/` - Shared utilities (JwtUtil, etc.)
- `enums/` - Enumeration types

### Key Patterns
- **WebSocket Protocol**: JSON messages with `type` field (auth, heartbeat, send_message, read_message, recall_message)
- **JWT Auth**: Access token (15min) + Refresh token (7d), stored in localStorage on frontend
- **AI Integration**: Strategy pattern with MiniMaxService, GlmService, DeepSeekService implementing AiService interface
- **Real-time Updates**: WebSocket pushed messages + polling for notifications

## Configuration

- **Frontend proxy**: Vite proxies `/api` → `localhost:19082` and `/ws` → `ws://localhost:19082`
- **Backend server**: Port 19082 (configured in `application.yml`)
- **MyBatis-Plus**: Logic delete enabled (`deleted` field), auto-id, underscore-to-camel mapping

import { describe, it, expect } from 'vitest'
import type {
  User,
  Message,
  Conversation,
  GroupInfo,
  GroupMember,
  FriendRequest,
  Robot,
  AiConfig,
  Notification,
  WsMessage
} from '@/types'

describe('types', () => {
  it('User should have required fields', () => {
    const user: User = {
      id: 1,
      username: 'testuser',
      status: 1,
      userStatus: 'ONLINE'
    }

    expect(user.id).toBe(1)
    expect(user.username).toBe('testuser')
    expect(user.status).toBe(1)
    expect(user.userStatus).toBe('ONLINE')
  })

  it('Message should have required fields', () => {
    const msg: Message = {
      id: 1,
      conversationId: 1,
      senderId: 1,
      content: 'Hello',
      contentType: 'TEXT',
      status: 'SENT',
      createTime: '2024-01-01T00:00:00Z'
    }

    expect(msg.id).toBe(1)
    expect(msg.content).toBe('Hello')
    expect(msg.contentType).toBe('TEXT')
    expect(msg.status).toBe('SENT')
  })

  it('Conversation should support SINGLE and GROUP types', () => {
    const singleConv: Conversation = {
      id: 1,
      type: 'SINGLE',
      typeId: 2,
      userId: 1,
      lastMessageId: 0,
      unreadCount: 0
    }

    const groupConv: Conversation = {
      id: 2,
      type: 'GROUP',
      typeId: 1,
      userId: 1,
      lastMessageId: 0,
      unreadCount: 0
    }

    expect(singleConv.type).toBe('SINGLE')
    expect(groupConv.type).toBe('GROUP')
  })

  it('GroupInfo should have required fields', () => {
    const group: GroupInfo = {
      id: 1,
      name: 'Test Group',
      type: 'PUBLIC',
      ownerId: 1,
      needAudit: false
    }

    expect(group.name).toBe('Test Group')
    expect(group.type).toBe('PUBLIC')
    expect(group.needAudit).toBe(false)
  })

  it('GroupMember should have role enum values', () => {
    const member: GroupMember = {
      id: 1,
      groupId: 1,
      userId: 1,
      role: 'OWNER',
      muted: 0,
      joinTime: '2024-01-01T00:00:00Z'
    }

    expect(member.role).toBe('OWNER')
  })

  it('FriendRequest should have status enum values', () => {
    const request: FriendRequest = {
      id: 1,
      fromUserId: 1,
      toUserId: 2,
      status: 'PENDING',
      createTime: '2024-01-01T00:00:00Z'
    }

    expect(request.status).toBe('PENDING')
  })

  it('Robot should have type and status enum values', () => {
    const robot: Robot = {
      id: 1,
      name: 'Test Bot',
      type: 'AI',
      ownerId: 1,
      status: 'ACTIVE',
      responseMode: 'sync',
      contextSize: 10
    }

    expect(robot.type).toBe('AI')
    expect(robot.status).toBe('ACTIVE')
  })

  it('AiConfig should have provider enum values', () => {
    const config: AiConfig = {
      id: 1,
      robotId: 1,
      provider: 'DEEPSEEK',
      model: 'deepseek-chat',
      apiKey: 'test-key',
      temperature: 0.7,
      maxTokens: 2000
    }

    expect(config.provider).toBe('DEEPSEEK')
  })

  it('Notification should have required fields', () => {
    const notif: Notification = {
      id: 1,
      userId: 1,
      type: 'MESSAGE',
      isRead: false,
      createTime: '2024-01-01T00:00:00Z'
    }

    expect(notif.isRead).toBe(false)
    expect(notif.type).toBe('MESSAGE')
  })

  it('WsMessage should have type and data fields', () => {
    const wsMsg: WsMessage = {
      type: 'message',
      data: { id: 1, content: 'Hello' }
    }

    expect(wsMsg.type).toBe('message')
    expect(wsMsg.data).toEqual({ id: 1, content: 'Hello' })
  })
})

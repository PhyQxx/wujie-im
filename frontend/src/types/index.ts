export interface User {
  id: number
  username: string
  phone?: string
  email?: string
  status: number
  userStatus: 'ONLINE' | 'OFFLINE' | 'AWAY' | 'DND'
  lastActiveTime?: string
  avatar?: string
  nickname?: string
  signature?: string
}

export interface Message {
  id: number
  conversationId: number
  senderId: number
  content: string
  contentType: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM' | 'CODE'
  meta?: any
  status: 'SENDING' | 'SENT' | 'DELIVERED' | 'READ' | 'RECALLED'
  recall?: boolean
  replyId?: number
  createTime: string
}

export interface Conversation {
  id: number
  type: 'SINGLE' | 'GROUP'
  typeId: number
  userId: number
  lastMessageId: number
  lastMessageContent?: string
  lastMessageTime?: string
  unreadCount: number
  targetUser?: User
  groupInfo?: GroupInfo
}

export interface GroupInfo {
  id: number
  name: string
  avatar?: string
  announcement?: string
  type: 'PUBLIC' | 'PRIVATE'
  ownerId: number
  needAudit: boolean
}

export interface GroupMember {
  id: number
  groupId: number
  userId: number
  role: 'OWNER' | 'ADMIN' | 'MEMBER'
  muted: boolean
  mutedUntil?: string
  joinTime: string
  user?: User
}

export interface FriendRequest {
  id: number
  fromUserId: number
  toUserId: number
  reason?: string
  status: 'PENDING' | 'AGREED' | 'REJECTED'
  createTime: string
  fromUser?: User
}

export interface Robot {
  id: number
  name: string
  avatar?: string
  description?: string
  type: 'AI' | 'CUSTOM'
  ownerId: number
  status: 'ACTIVE' | 'INACTIVE'
  responseMode: string
  contextSize: number
  aiConfig?: AiConfig
}

export interface AiConfig {
  id: number
  robotId: number
  provider: 'MINIMAX' | 'GLM' | 'DEEPSEEK'
  model: string
  apiKey: string
  systemPrompt?: string
  temperature: number
  maxTokens: number
}

export interface RobotRule {
  id: number
  robotId: number
  ruleType: 'KEYWORD' | 'REGEX' | 'WEBHOOK'
  keyword?: string
  pattern?: string
  webhookUrl?: string
  replyContent?: string
  replyType: string
  priority: number
  enabled: boolean
}

export interface Notification {
  id: number
  userId: number
  type: string
  title?: string
  content?: string
  sourceId?: number
  isRead: boolean
  createTime: string
}

export interface WsMessage {
  type: string
  data: any
}

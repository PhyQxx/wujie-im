-- 数据库索引优化脚本
-- 为高频查询字段添加索引，提升查询性能

-- message 表索引
CREATE INDEX IF NOT EXISTS idx_message_conversation_id ON message(conversation_id);
CREATE INDEX IF NOT EXISTS idx_message_sender_id ON message(sender_id);
CREATE INDEX IF NOT EXISTS idx_message_create_time ON message(create_time);
CREATE INDEX IF NOT EXISTS idx_message_conversation_recall ON message(conversation_id, recall);
CREATE INDEX IF NOT EXISTS idx_message_conversation_time ON message(conversation_id, create_time DESC);

-- conversation 表索引
CREATE INDEX IF NOT EXISTS idx_conversation_user_id ON conversation(user_id);
CREATE INDEX IF NOT EXISTS idx_conversation_user_type ON conversation(user_id, type, type_id);
CREATE INDEX IF NOT EXISTS idx_conversation_last_message_time ON conversation(last_message_time);

-- friend_relation 表索引
CREATE INDEX IF NOT EXISTS idx_friend_user_id ON friend_relation(user_id);
CREATE INDEX IF NOT EXISTS idx_friend_target_user ON friend_relation(user_id, friend_user_id);

-- friend_request 表索引
CREATE INDEX IF NOT EXISTS idx_friend_request_from ON friend_request(from_user_id);
CREATE INDEX IF NOT EXISTS idx_friend_request_to ON friend_request(to_user_id);
CREATE INDEX IF NOT EXISTS idx_friend_request_status ON friend_request(to_user_id, status);

-- group_member 表索引
CREATE INDEX IF NOT EXISTS idx_group_member_group_id ON group_member(group_id);
CREATE INDEX IF NOT EXISTS idx_group_member_user_id ON group_member(user_id);
CREATE INDEX IF NOT EXISTS idx_group_member_group_user ON group_member(group_id, user_id);

-- group_info 表索引
CREATE INDEX IF NOT EXISTS idx_group_info_owner ON group_info(owner_id);

-- message_read 表索引
CREATE INDEX IF NOT EXISTS idx_message_read_user_conv ON message_read(user_id, conversation_id);
CREATE INDEX IF NOT EXISTS idx_message_read_user ON message_read(user_id);

-- notification 表索引
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notification(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_user_read ON notification(user_id, is_read);

-- robot 表索引
CREATE INDEX IF NOT EXISTS idx_robot_owner ON robot(owner_id);

-- user 表索引
CREATE INDEX IF NOT EXISTS idx_user_username ON user(username);
CREATE INDEX IF NOT EXISTS idx_user_status ON user(status);

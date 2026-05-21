-- ===== 1. ai_config 表重构：robot_id → name，增加 api_url =====

-- 添加 name 和 api_url 字段
ALTER TABLE ai_config ADD COLUMN name VARCHAR(100) COMMENT '配置名称' AFTER id;
ALTER TABLE ai_config ADD COLUMN api_url VARCHAR(500) COMMENT '自定义API地址' AFTER api_key;

-- 删除 robot_id 外键约束后再删字段
-- 注意：先确认外键名，可先执行 SHOW CREATE TABLE ai_config 查看
-- 以下外键名以 ai_config_ibfk_1 为例（根据实际报错信息确认）
ALTER TABLE ai_config DROP FOREIGN KEY ai_config_ibfk_1;
ALTER TABLE ai_config DROP COLUMN robot_id;

-- ===== 2. robot 表增加字段 =====

ALTER TABLE robot ADD COLUMN ai_config_id BIGINT COMMENT '关联的AI配置ID' AFTER owner_id;
ALTER TABLE robot ADD COLUMN virtual_user_id BIGINT COMMENT '机器人虚拟用户ID' AFTER ai_config_id;

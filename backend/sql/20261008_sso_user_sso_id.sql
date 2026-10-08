-- SSO：pnkx 统一登录账号关联（手动执行）
-- sso_id = pnkx sys_user.user_id（OIDC sub），首次 SSO 登录 JIT 建号写入；
-- 不自动绑定同名本地账号（防接管）。
ALTER TABLE `user`
    ADD COLUMN `sso_id` VARCHAR(64) NULL COMMENT 'pnkx 统一登录ID（OIDC sub）' AFTER `username`;

CREATE UNIQUE INDEX `uk_user_sso_id` ON `user` (`sso_id`);

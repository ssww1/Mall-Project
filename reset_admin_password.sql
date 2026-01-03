-- ========================================
-- 管理员密码重置 SQL
-- ========================================
-- 说明：将管理员（username='admin'）的密码重置为 "admin"（BCrypt 加密）
-- 
-- 使用方法：
-- 1. 在 MySQL 中连接到 mall 数据库
-- 2. 执行以下 SQL 语句
-- 3. 完成后，可以使用用户名 "admin" 和密码 "admin" 登录后台
-- ========================================

-- 方法1：使用预生成的 BCrypt 密码（密码为 "admin"）
-- 注意：BCrypt 每次加密结果不同，如果这个不工作，请使用方法2
UPDATE admin_user 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'admin';

-- 方法2：如果方法1不工作，请在 IDEA 中运行 PasswordResetUtil.java 的 main 方法
-- 它会生成新的 BCrypt 密码，然后替换上面的 UPDATE 语句中的密码值

-- 验证：查询更新后的结果（密码应该是 BCrypt 格式，以 $2a$ 开头）
SELECT id, username, password FROM admin_user WHERE username = 'admin';


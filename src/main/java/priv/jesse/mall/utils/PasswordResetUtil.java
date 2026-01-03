package priv.jesse.mall.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码重置工具类。
 * <p>
 * 用于生成 BCrypt 加密后的密码，便于数据库迁移或密码重置。
 * </p>
 * <p>
 * 使用方式：
 * </p>
 * <ol>
 * <li>运行 main 方法，输入要重置的明文密码（如 "admin"）</li>
 * <li>复制输出的 BCrypt 密文</li>
 * <li>执行 SQL：UPDATE admin_user SET password = 'BCrypt密文' WHERE username = 'admin';</li>
 * </ol>
 */
public class PasswordResetUtil {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 生成指定明文密码的 BCrypt 加密字符串。
     *
     * @param rawPassword 明文密码
     * @return BCrypt 加密后的字符串
     */
    public static String generateBCryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 主方法：用于生成 BCrypt 密码。
     * <p>
     * 运行此方法，输入要加密的密码，会输出对应的 BCrypt 密文和 SQL 更新语句。
     * </p>
     */
    public static void main(String[] args) {
        // 要重置的明文密码（默认：admin）
        String rawPassword = "admin";

        // 生成 BCrypt 加密密码
        String bcryptPassword = generateBCryptPassword(rawPassword);

        // 输出结果
        System.out.println("========================================");
        System.out.println("密码重置工具");
        System.out.println("========================================");
        System.out.println("明文密码: " + rawPassword);
        System.out.println("BCrypt 密文: " + bcryptPassword);
        System.out.println();
        System.out.println("SQL 更新语句（管理员）:");
        System.out.println("UPDATE admin_user SET password = '" + bcryptPassword + "' WHERE username = 'admin';");
        System.out.println();
        System.out.println("SQL 更新语句（普通用户，可选）:");
        System.out.println("UPDATE user SET password = '" + bcryptPassword + "' WHERE username = 'your_username';");
        System.out.println("========================================");
    }
}


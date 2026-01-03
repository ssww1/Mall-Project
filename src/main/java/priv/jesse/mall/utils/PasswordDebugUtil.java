package priv.jesse.mall.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码调试工具类。
 * <p>
 * 用于排查密码验证问题，检查 BCrypt 密文是否正确。
 * </p>
 */
public class PasswordDebugUtil {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 测试给定的 BCrypt 密文是否能验证指定的明文密码。
     *
     * @param bcryptHash BCrypt 密文（从数据库读取）
     * @param rawPassword 明文密码（用户输入的密码）
     */
    public static void testPassword(String bcryptHash, String rawPassword) {
        System.out.println("========================================");
        System.out.println("密码验证调试工具");
        System.out.println("========================================");
        System.out.println("BCrypt 密文: " + bcryptHash);
        System.out.println("密文长度: " + (bcryptHash != null ? bcryptHash.length() : 0) + " 字符");
        System.out.println("明文密码: " + rawPassword);
        System.out.println();

        // 检查密文格式
        if (bcryptHash == null || bcryptHash.isEmpty()) {
            System.out.println("❌ 错误：BCrypt 密文为空！");
            return;
        }

        if (!bcryptHash.startsWith("$2a$") && !bcryptHash.startsWith("$2b$")) {
            System.out.println("❌ 错误：BCrypt 密文格式不正确！");
            System.out.println("   应该以 $2a$ 或 $2b$ 开头");
            return;
        }

        if (bcryptHash.length() != 60) {
            System.out.println("⚠️  警告：BCrypt 密文长度不是60字符（当前：" + bcryptHash.length() + "）");
            System.out.println("   可能被截断或包含额外字符");
        }

        // 尝试验证
        try {
            boolean matches = passwordEncoder.matches(rawPassword, bcryptHash);
            if (matches) {
                System.out.println("✅ 验证成功：密码匹配！");
            } else {
                System.out.println("❌ 验证失败：密码不匹配！");
                System.out.println();
                System.out.println("可能的原因：");
                System.out.println("1. 数据库中的密文不正确（可能被截断或修改）");
                System.out.println("2. 明文密码输入错误");
                System.out.println("3. 密文在存储/读取过程中被修改（如 trim、转义等）");
            }
        } catch (Exception e) {
            System.out.println("❌ 验证过程出错：" + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("========================================");
    }

    /**
     * 主方法：用于调试密码问题。
     * <p>
     * 使用方法：
     * </p>
     * <ol>
     * <li>从数据库查询管理员密码：SELECT password FROM admin_user WHERE username = 'admin';</li>
     * <li>复制查询结果（BCrypt 密文）</li>
     * <li>运行此方法，第一个参数是 BCrypt 密文，第二个参数是明文密码（如 "admin"）</li>
     * </ol>
     */
    public static void main(String[] args) {
        // 示例：测试密码验证
        // 第一个参数：从数据库读取的 BCrypt 密文
        // 第二个参数：用户输入的明文密码
        String bcryptHashFromDB = args.length > 0 ? args[0] : null;
        String rawPassword = args.length > 1 ? args[1] : "admin";

        if (bcryptHashFromDB == null) {
            System.out.println("使用方法：");
            System.out.println("java PasswordDebugUtil <BCrypt密文> <明文密码>");
            System.out.println();
            System.out.println("示例：");
            System.out.println("java PasswordDebugUtil '$2a$10$...' admin");
            System.out.println();
            System.out.println("或者直接修改代码中的 bcryptHashFromDB 变量");
            return;
        }

        testPassword(bcryptHashFromDB, rawPassword);
    }
}


package priv.jesse.mall.utils;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt 工作原理测试。
 * <p>
 * 用于演示为什么 BCrypt 每次加密结果不同，但还能正确验证密码。
 * </p>
 */
public class BCryptTest {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void testBCryptHowItWorks() {
        String rawPassword = "admin";

        // 第一次加密
        String hash1 = passwordEncoder.encode(rawPassword);
        System.out.println("第一次加密结果: " + hash1);
        System.out.println("长度: " + hash1.length());

        // 第二次加密（同样的密码）
        String hash2 = passwordEncoder.encode(rawPassword);
        System.out.println("第二次加密结果: " + hash2);
        System.out.println("长度: " + hash2.length());

        // 验证：两个不同的哈希值都能验证同一个密码
        boolean match1 = passwordEncoder.matches(rawPassword, hash1);
        boolean match2 = passwordEncoder.matches(rawPassword, hash2);
        boolean match3 = passwordEncoder.matches(rawPassword, hash1); // 再次验证 hash1

        System.out.println();
        System.out.println("========================================");
        System.out.println("BCrypt 工作原理说明：");
        System.out.println("========================================");
        System.out.println("1. 每次加密结果不同（因为盐值随机）");
        System.out.println("2. 但都能验证同一个明文密码");
        System.out.println("3. 盐值存储在哈希字符串的前29个字符中");
        System.out.println();
        System.out.println("验证结果：");
        System.out.println("hash1 验证 'admin': " + match1);
        System.out.println("hash2 验证 'admin': " + match2);
        System.out.println("hash1 再次验证 'admin': " + match3);
        System.out.println();
        System.out.println("结论：虽然 hash1 != hash2，但都能验证 'admin'");
        System.out.println("========================================");
    }

    @Test
    public void testBCryptStructure() {
        String rawPassword = "admin";
        String hash = passwordEncoder.encode(rawPassword);

        System.out.println("BCrypt 哈希结构分析：");
        System.out.println("完整哈希: " + hash);
        System.out.println("长度: " + hash.length() + " 字符");
        System.out.println();
        System.out.println("结构说明：");
        System.out.println("$2a$10$" + "..." + " 前7个字符：算法标识和成本因子");
        System.out.println("接下来的22个字符：盐值（Base64编码）");
        System.out.println("最后31个字符：实际密码哈希值");
        System.out.println();
        System.out.println("验证时，BCrypt 会：");
        System.out.println("1. 从哈希中提取盐值");
        System.out.println("2. 用这个盐值对输入的密码进行哈希");
        System.out.println("3. 比较结果是否匹配");
    }
}


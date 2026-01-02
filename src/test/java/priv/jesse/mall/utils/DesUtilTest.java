package priv.jesse.mall.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * DesUtil 工具类单元测试
 *
 * 覆盖点：
 * 1. 加密 -> 解密结果应与原文一致（正向互逆）
 * 2. decrypt 传入 null 应返回 null，保证空安全
 */
public class DesUtilTest {

    private static final String KEY = "wow!@#$%"; // 8 字节

    @Test
    public void encryptAndDecrypt_ShouldBeReversible() throws Exception {
        String origin = "Mall@2026";
        String cipher = DesUtil.encrypt(origin, KEY);
        String plain = DesUtil.decrypt(cipher, KEY);
        Assert.assertEquals(origin, plain);
    }

    @Test
    public void decrypt_NullInput_ReturnNull() throws Exception {
        Assert.assertNull(DesUtil.decrypt(null, KEY));
    }
}


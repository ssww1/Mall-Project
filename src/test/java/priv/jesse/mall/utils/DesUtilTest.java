package priv.jesse.mall.utils;

import org.junit.Assert;
import org.junit.Test;

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


package priv.jesse.mall.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * DES 加密/解密工具类（演示用途）。
 *
 * <p>实现：DES 对称加密 + Base64 编码输出。</p>
 * <p>注意：DES 已不推荐用于生产环境（密钥长度较短且使用 ECB 默认模式可能存在安全风险），
 * 建议替换为 AES + CBC/GCM 或使用 BCrypt（密码场景）。</p>
 */
public class DesUtil {
    private final static String DES = "DES";

    /**
     * 用于本地手工验证的 main 方法（非业务逻辑）。
     */
    public static void main(String[] args) throws Exception {
        String data = "123 456";
        String key = "wow!@#$%";
        System.err.println(encrypt(data, key));
        System.err.println(decrypt(encrypt(data, key), key));
    }

    /**
     * 根据 key 对明文进行加密。
     *
     * @param data 明文
     * @param key  密钥（字节数组长度必须满足 DES 要求，通常为 8 字节）
     * @return Base64 编码后的密文
     */
    public static String encrypt(String data, String key) throws Exception {
        // 1) 执行 DES 加密，得到二进制密文
        byte[] bt = encrypt(data.getBytes(), key.getBytes());
        // 2) 转为 Base64 文本，便于存储/传输
        return new BASE64Encoder().encode(bt);
    }

    /**
     * 根据 key 对密文进行解密。
     *
     * @param data Base64 编码后的密文
     * @param key  密钥
     * @return 明文
     */
    public static String decrypt(String data, String key) throws IOException, Exception {
        if (data == null) {
            return null;
        }

        // 1) Base64 解码为二进制密文
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] buf = decoder.decodeBuffer(data);

        // 2) DES 解密
        byte[] bt = decrypt(buf, key.getBytes());
        return new String(bt);
    }

    /**
     * DES 加密底层实现。
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源（用于 Cipher 初始化）
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建 DESKeySpec
        DESKeySpec dks = new DESKeySpec(key);

        // 密钥工厂：把 DESKeySpec 转换为 SecretKey
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher 实际完成加密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 使用密钥初始化 Cipher（加密模式）
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }

    /**
     * DES 解密底层实现。
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance(DES);

        // 使用密钥初始化 Cipher（解密模式）
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }
}

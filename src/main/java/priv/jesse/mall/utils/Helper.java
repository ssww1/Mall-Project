package priv.jesse.mall.utils;

/**
 * 通用辅助工具类。
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>字符串数组包含判断</li>
 *   <li>使用固定 key 的 DES 加解密包装（encode/decode）</li>
 *   <li>byte[] 转十六进制字符串（用于生成 MD5 文件名等）</li>
 * </ul>
 *
 * <p>注意：固定 key + DES 仅用于演示；生产建议使用更安全的加密方案。</p>
 */
public class Helper {

    /**
     * DES 加解密固定 key（演示用途）。
     */
    private static String key = "wow!@#$%";

    /**
     * 判断字符串是否存在于数组中。
     */
    public static boolean isStringInArray(String str, String[] array) {
        for (String val : array) {
            if (str.equals(val)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用固定 key 对字符串进行加密（DES + Base64）。
     *
     * @param str 明文
     * @return 密文（Base64 文本）；异常时返回空字符串
     */
    public static String encode(String str) {
        String enStr = "";
        try {
            enStr = DesUtil.encrypt(str, key);
        } catch (Exception e) {
            // 演示项目直接打印堆栈；生产环境应记录日志并返回统一错误
            e.printStackTrace();
        }
        return enStr;
    }

    /**
     * 使用固定 key 对字符串进行解密。
     *
     * @param str 密文（Base64 文本）
     * @return 明文；异常时返回空字符串
     */
    public static String decode(String str) {
        String deStr = "";
        try {
            deStr = DesUtil.decrypt(str, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deStr;
    }

    /**
     * 将 bytes[start, start+end) 转换为十六进制字符串。
     *
     * <p>常用于把 MD5 digest 输出为字符串文件名。</p>
     */
    public static String bytesToHex(byte bytes[], int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + end; i++) {
            sb.append(byteToHex(bytes[i]));
        }
        return sb.toString();
    }

    /**
     * 16 进制字符集。
     */
    private static final char HEX_DIGITS[] = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * 将单个字节转换为 2 位十六进制字符串。
     *
     * @param bt 目标字节
     * @return 形如 "0A"、"FF" 的字符串
     */
    public static String byteToHex(byte bt) {
        // bt & 0xf0 取高 4 位，bt & 0x0f 取低 4 位
        return HEX_DIGITS[(bt & 0xf0) >> 4] + "" + HEX_DIGITS[bt & 0xf];
    }
}

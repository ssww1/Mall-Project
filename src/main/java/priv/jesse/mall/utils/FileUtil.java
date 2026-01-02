package priv.jesse.mall.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;

/**
 * 文件上传工具类。
 *
 * <p>核心功能：保存上传的 MultipartFile 到本地 file/ 目录，
 * 并返回一个可通过 Web 访问的 URL。</p>
 */
public class FileUtil {

    /**
     * 保存上传的文件。
     *
     * <p>实现逻辑：</p>
     * <ol>
     *   <li>计算文件内容的 MD5 哈希作为文件名（不含后缀），保证内容相同的文件名也相同。</li>
     *   <li>保留原始文件后缀。</li>
     *   <li>将文件写入项目根目录下的 file/ 文件夹。</li>
     *   <li>使用 {@link StandardOpenOption#CREATE_NEW} 写入，如果文件已存在会抛异常，防止覆盖。</li>
     * </ol>
     *
     * @param file Spring MVC 的 MultipartFile 对象
     * @return 文件下载的 URL，格式如 /mall/admin/product/img/FILENAME.EXT
     * @throws Exception 文件读写或 MD5 计算异常
     */
    public static String saveFile(MultipartFile file) throws Exception {
        // 1) 空文件或 null 直接返回空字符串，不处理
        if (file == null || file.isEmpty()) {
            return "";
        }

        // 2) 确保目标目录存在
        File targetDir = new File("file");
        if (!targetDir.isDirectory()) {
            targetDir.mkdirs();
        }

        // 3) 生成文件名：MD5(文件内容) + "." + 后缀
        String originalFilename = file.getOriginalFilename();
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(file.getBytes());
        String fileName = (Helper.bytesToHex(md.digest(), 0, md.digest().length - 1)) + "." + getPostfix(originalFilename);

        // 4) 写入文件
        File destFile = new File(targetDir.getPath() + "/" + fileName);
        //    CREATE_NEW 保证原子性创建，若文件已存在则抛 FileAlreadyExistsException
        Files.write(Paths.get(destFile.toURI()), file.getBytes(), StandardOpenOption.CREATE_NEW);

        // 5) 返回可供 Controller 映射的 URL
        return "/mall/admin/product/img/" + fileName;
    }

    /**
     * 获取文件后缀名（不含点）。
     *
     * @param fileName 原始文件名
     * @return 后缀名，或在无后缀/空文件名时返回空字符串
     */
    public static String getPostfix(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "";
        }
        if (fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "";
    }

}

package priv.jesse.mall.utils;

import org.junit.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * FileUtil.saveFile 单元测试
 *
 * 场景：
 * 1. 空文件 / null 输入 → 返回空字符串
 * 2. 大文件（>2MB）写入成功
 * 3. 重复内容文件第二次写入触发 FileAlreadyExistsException
 */
public class FileUtilTest {

    private static final String DIR = "file"; // FileUtil 默认目录

    @After
    public void cleanUp() throws Exception {
        // 删除 file/ 目录，保持测试幂等
        FileSystemUtils.deleteRecursively(new File(DIR));
    }

    /**************** 1. 空文件 *****************/
    @Test
    public void saveFile_nullOrEmpty_returnEmptyString() throws Exception {
        // null
        Assert.assertEquals("", FileUtil.saveFile(null));

        // empty bytes
        MockMultipartFile empty = new MockMultipartFile("img", "empty.png", "image/png", new byte[] {});
        Assert.assertEquals("", FileUtil.saveFile(empty));
    }

    /**************** 2. 大文件 (>2MB) *****************/
    @Test
    public void saveFile_largeFile_success() throws Exception {
        // 构造 3MB 数据
        byte[] data = new byte[3 * 1024 * 1024];
        MockMultipartFile big = new MockMultipartFile("img", "big.jpg", "image/jpeg", data);

        String url = FileUtil.saveFile(big);
        Assert.assertTrue(url.startsWith("/mall/admin/product/img/"));

        // 验证文件确实写入
        String filename = url.substring(url.lastIndexOf('/') + 1);
        Path path = Paths.get(DIR, filename);
        Assert.assertTrue("file should exist", Files.exists(path));
        Assert.assertEquals(data.length, Files.size(path));
    }

    /**************** 3. 重复文件名（相同内容） *****************/
    @Test(expected = java.nio.file.FileAlreadyExistsException.class)
    public void saveFile_duplicateContent_throwException() throws Exception {
        byte[] data = "duplicate".getBytes();
        MockMultipartFile file1 = new MockMultipartFile("img", "dup.png", "image/png", data);
        MockMultipartFile file2 = new MockMultipartFile("img", "dup.png", "image/png", data);

        // 第一次保存成功
        FileUtil.saveFile(file1);
        // 第二次保存相同 MD5 -> 相同文件名，期望抛 FileAlreadyExistsException
        FileUtil.saveFile(file2);
    }
}

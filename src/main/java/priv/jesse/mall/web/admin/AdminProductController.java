package priv.jesse.mall.web.admin;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import priv.jesse.mall.entity.Classification;
import priv.jesse.mall.entity.Product;
import priv.jesse.mall.entity.pojo.ResultBean;
import priv.jesse.mall.service.ClassificationService;
import priv.jesse.mall.service.ProductService;
import priv.jesse.mall.utils.FileUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 后台管理：商品管理。
 *
 * <p>主要能力：</p>
 * <ul>
 *   <li>商品列表分页查询</li>
 *   <li>新增/编辑商品（包含图片上传）</li>
 *   <li>删除商品</li>
 *   <li>通过 /img/{filename} 下载或读取上传图片</li>
 * </ul>
 *
 * <p>注意：图片实际存储在项目根目录的 file/ 目录下，URL 通过 {@link priv.jesse.mall.utils.FileUtil} 生成。</p>
 */
@Controller
@RequestMapping("/admin/product")
public class AdminProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ClassificationService classificationService;

    /**
     * 打开商品列表页。
     */
    @RequestMapping("/toList.html")
    public String toList() {
        // 返回 templates/admin/product/list.html
        return "admin/product/list";
    }

    /**
     * 打开新增商品页面。
     */
    @RequestMapping("/toAdd.html")
    public String toAdd() {
        // 返回 templates/admin/product/add.html
        return "admin/product/add";
    }

    /**
     * 打开编辑商品页面。
     *
     * @param id 商品 id
     * @param map Model，用于向页面传递商品与分类信息
     */
    @RequestMapping("/toEdit.html")
    public String toEdit(int id, Map<String, Object> map) {
        // 1) 查询商品信息
        Product product = productService.findById(id);

        // 2) 查询商品所属二级分类，并回填到 product 里（供页面展示分类名称）
        //    注意：Product 中保存的是 csid（二级分类 id），页面需要分类名称
        Classification classification = classificationService.findById(product.getCsid());
        product.setCategorySec(classification);

        // 3) 放入 Model
        map.put("product", product);

        // 4) 返回编辑模板
        return "admin/product/edit";
    }

    /**
     * 分页获取商品列表（供后台列表页 Ajax 调用）。
     *
     * @param pageindex 页码，从 0 开始
     * @param pageSize  每页数量
     */
    @ResponseBody
    @RequestMapping("/list.do")
    public ResultBean<List<Product>> listProduct(int pageindex,
                                                 @RequestParam(value = "pageSize", defaultValue = "15") int pageSize) {
        // 1) 构建分页查询条件
        Pageable pageable = new PageRequest(pageindex, pageSize, null);

        // 2) 查询分页结果，仅取当前页内容
        List<Product> list = productService.findAll(pageable).getContent();

        // 3) ResultBean 统一包装返回给前端
        return new ResultBean<>(list);
    }

    /**
     * 获取商品总数（用于前端分页组件计算总页数）。
     */
    @ResponseBody
    @RequestMapping("/getTotal")
    public ResultBean<Integer> getTotal() {
        // 这里用任意 pageable 查询总数即可
        Pageable pageable = new PageRequest(1, 15, null);
        int total = (int) productService.findAll(pageable).getTotalElements();
        return new ResultBean<>(total);
    }

    /**
     * 删除商品。
     *
     * @param id 商品 id
     */
    @RequestMapping("/del.do")
    @ResponseBody
    public ResultBean<Boolean> del(int id) {
        // 直接按 id 删除
        productService.delById(id);
        return new ResultBean<>(true);
    }

    /**
     * 新增商品（包含图片上传）。
     *
     * <p>关键点：</p>
     * <ul>
     *   <li>图片通过 FileUtil 保存到 file/ 目录，并返回可访问 URL</li>
     *   <li>新增成功后 forward 到编辑页，便于继续修改</li>
     *   <li>新增失败 forward 回新增页，并在 request 中携带 message</li>
     * </ul>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/add.do")
    public void add(MultipartFile image,
                    String title,
                    Double marketPrice,
                    Double shopPrice,
                    int isHot,
                    String desc,
                    int csid,
                    HttpServletRequest request,
                    HttpServletResponse response) throws Exception {
        // 1) 组装商品对象（后台表单提交）
        Product product = new Product();
        product.setTitle(title);
        product.setMarketPrice(marketPrice);
        product.setShopPrice(shopPrice);
        product.setDesc(desc);
        product.setIsHot(isHot); // 1=热门，0=非热门
        product.setCsid(csid);   // 二级分类 id
        product.setPdate(new Date());

        // 2) 保存上传图片到 file/ 目录，并生成可访问的图片 URL
        //    如果用户未选择图片，FileUtil.saveFile 会返回 ""（空字符串）
        String imgUrl = FileUtil.saveFile(image);
        product.setImage(imgUrl);

        // 3) 保存商品记录到数据库
        int id = productService.create(product);

        // 4) 根据创建结果 forward 到不同页面（注意：这里是 forward 不是 redirect）
        if (id <= 0) {
            // 创建失败：回到新增页，并在 request 上携带提示信息
            request.setAttribute("message", "添加失败！");
            request.getRequestDispatcher("toAdd.html").forward(request, response);
        } else {
            // 创建成功：直接 forward 到编辑页，便于继续补充/修改信息
            request.getRequestDispatcher("toEdit.html?id=" + id).forward(request, response);
        }
    }


    /**
     * 更新商品信息。
     *
     * <p>关键分支：</p>
     * <ul>
     *   <li>如果用户重新上传了图片：更新 image 字段</li>
     *   <li>如果未上传图片：保持原图片不变</li>
     * </ul>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/update.do")
    public void update(int id,
                       String title,
                       Double marketPrice,
                       Double shopPrice,
                       String desc,
                       int csid,
                       int isHot,
                       MultipartFile image,
                       HttpServletRequest request,
                       HttpServletResponse response) throws Exception {
        // 1) 先查询原商品（保证更新的是同一条记录，同时保留未修改字段）
        Product product = productService.findById(id);

        // 2) 更新表单字段
        product.setTitle(title);
        product.setMarketPrice(marketPrice);
        product.setShopPrice(shopPrice);
        product.setDesc(desc);
        product.setIsHot(isHot);
        product.setCsid(csid);
        // 更新日期（作为“最近修改时间”使用）
        product.setPdate(new Date());

        // 3) 处理图片：若上传了新图片，则更新 image 字段
        String imgUrl = FileUtil.saveFile(image);
        if (StringUtils.isNotBlank(imgUrl)) {
            product.setImage(imgUrl);
        }

        // 4) 执行保存
        boolean flag = false;
        try {
            productService.update(product);
            flag = true;
        } catch (Exception e) {
            // 这里捕获后重新抛出，最终会被 GlobalExceptionHandler 统一处理
            throw new Exception(e);
        }

        // 5) 若失败，将提示信息写入 request（但此处随后会 redirect，提示可能丢失）
        if (!flag) {
            request.setAttribute("message", "更新失败！");
        }

        // 6) 更新后重定向到列表页（避免表单重复提交）
        response.sendRedirect("toList.html");
    }

    /**
     * 读取/下载商品图片。
     *
     * <p>图片保存路径：项目根目录 file/{filename}</p>
     * <p>返回策略：设置下载响应头并将文件流写入 Response OutputStream。</p>
     */
    @RequestMapping(method = RequestMethod.GET, value = "/img/{filename:.+}")
    public void getImage(@PathVariable(name = "filename", required = true) String filename,
                         HttpServletResponse res) throws IOException {
        // 1) 拼接本地文件路径
        File file = new File("file/" + filename);

        // 2) 仅当文件存在时才输出；否则保持空响应（可按需扩展返回 404）
        if (file != null && file.exists()) {
            // 3) 设置响应头：以附件形式下载
            res.setHeader("content-type", "application/octet-stream");
            res.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            res.setContentLengthLong(file.length());

            // 4) 复制文件到响应输出流
            Files.copy(Paths.get(file.toURI()), res.getOutputStream());
        }
    }

}

package priv.jesse.mall.web.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import priv.jesse.mall.entity.Classification;
import priv.jesse.mall.entity.OrderItem;
import priv.jesse.mall.entity.Product;
import priv.jesse.mall.entity.pojo.ResultBean;
import priv.jesse.mall.service.ClassificationService;
import priv.jesse.mall.service.ProductService;
import priv.jesse.mall.service.ShopCartService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 前台商品模块：商品详情、热门/最新商品、按分类分页浏览、购物车相关接口。
 *
 * <p>说明：</p>
 * <ul>
 *   <li>商品查询相关接口返回 {@link ResultBean} JSON，供前端 Ajax 调用。</li>
 *   <li>购物车数据存放在 Session 中，核心逻辑由 {@link ShopCartService} 实现。</li>
 * </ul>
 */
@Controller
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ClassificationService classificationService;
    @Autowired
    private ShopCartService shopCartService;

    /**
     * 获取商品信息（JSON）。
     *
     * @param id 商品 id
     */
    @RequestMapping("/get.do")
    public ResultBean<Product> getProduct(int id) {
        // 直接按 id 查询商品
        Product product = productService.findById(id);
        return new ResultBean<>(product);
    }

    /**
     * 打开商品详情页面（HTML）。
     *
     * <p>流程：查询商品 -> 放入 model -> 返回详情模板。</p>
     */
    @RequestMapping("/get.html")
    public String toProductPage(int id, Map<String, Object> map) {
        // 1) 查询商品
        Product product = productService.findById(id);
        // 2) 放入页面模型
        map.put("product", product);
        // 3) 返回 templates/mall/product/info.html
        return "mall/product/info";
    }

    /**
     * 查询热门商品（JSON）。
     *
     * <p>热门商品一般由 isHot=1 标识（具体逻辑在 ProductService 中）。</p>
     */
    @ResponseBody
    @RequestMapping("/hot.do")
    public ResultBean<List<Product>> getHotProduct() {
        List<Product> products = productService.findHotProduct();
        return new ResultBean<>(products);
    }

    /**
     * 查询最新商品（JSON，分页）。
     *
     * @param pageNo   页码（从 0 开始）
     * @param pageSize 每页数量
     */
    @ResponseBody
    @RequestMapping("/new.do")
    public ResultBean<List<Product>> getNewProduct(int pageNo, int pageSize) {
        // 构造分页参数
        Pageable pageable = new PageRequest(pageNo, pageSize);
        // 调用 service 获取最新商品
        List<Product> products = productService.findNewProduct(pageable);
        return new ResultBean<>(products);
    }

    /**
     * 打开分类商品列表页面（HTML）。
     *
     * @param cid 一级分类 id
     */
    @RequestMapping("/category.html")
    public String toCatePage(int cid, Map<String, Object> map) {
        // 1) 查询一级分类信息
        Classification classification = classificationService.findById(cid);
        // 2) 放入 model（页面用于展示分类标题等）
        map.put("category", classification);
        // 3) 返回 templates/mall/product/category.html
        return "mall/product/category";
    }

    /**
     * 打开购物车页面（HTML）。
     */
    @RequestMapping("/toCart.html")
    public String toCart() {
        return "mall/product/cart";
    }

    /**
     * 按一级分类分页查询商品（JSON）。
     */
    @ResponseBody
    @RequestMapping("/category.do")
    public ResultBean<List<Product>> getCategoryProduct(int cid, int pageNo, int pageSize) {
        // 1) 构造分页条件
        Pageable pageable = new PageRequest(pageNo, pageSize);
        // 2) 调用 service 查询
        List<Product> products = productService.findByCid(cid, pageable);
        return new ResultBean<>(products);
    }

    /**
     * 按二级分类分页查询商品（JSON）。
     */
    @ResponseBody
    @RequestMapping("/categorySec.do")
    public ResultBean<List<Product>> getCategorySecProduct(int csId, int pageNo, int pageSize) {
        Pageable pageable = new PageRequest(pageNo, pageSize);
        List<Product> products = productService.findByCsid(csId, pageable);
        return new ResultBean<>(products);
    }

    /**
     * 根据一级分类查询其下所有二级分类（JSON）。
     *
     * @param cid 一级分类 id
     */
    @ResponseBody
    @RequestMapping("/getCategorySec.do")
    public ResultBean<List<Classification>> getCategorySec(int cid) {
        // parentId=cid 表示查询该一级分类的二级分类列表
        List<Classification> list = classificationService.findByParentId(cid);
        return new ResultBean<>(list);
    }

    /**
     * 加入购物车（JSON）。
     *
     * <p>购物车逻辑由 ShopCartService 实现，商品 id 仅写入 Session。</p>
     */
    @ResponseBody
    @RequestMapping("/addCart.do")
    public ResultBean<Boolean> addToCart(int productId, HttpServletRequest request) throws Exception {
        // 1) 将商品加入购物车（内部会检查登录态）
        shopCartService.addCart(productId, request);
        // 2) 返回 true 表示成功
        return new ResultBean<>(true);
    }

    /**
     * 从购物车移除商品（JSON）。
     */
    @ResponseBody
    @RequestMapping("/delCart.do")
    public ResultBean<Boolean> delToCart(int productId, HttpServletRequest request) throws Exception {
        shopCartService.remove(productId, request);
        return new ResultBean<>(true);
    }

    /**
     * 查看购物车商品列表（JSON）。
     *
     * <p>返回的是 OrderItem 列表：包含 count/subTotal/product 等信息。</p>
     */
    @ResponseBody
    @RequestMapping("/listCart.do")
    public ResultBean<List<OrderItem>> listCart(HttpServletRequest request) throws Exception {
        List<OrderItem> orderItems = shopCartService.listCart(request);
        return new ResultBean<>(orderItems);
    }
}

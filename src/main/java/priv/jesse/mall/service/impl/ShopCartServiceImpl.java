package priv.jesse.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.jesse.mall.entity.OrderItem;
import priv.jesse.mall.entity.Product;
import priv.jesse.mall.entity.User;
import priv.jesse.mall.service.ProductService;
import priv.jesse.mall.service.ShopCartService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 购物车业务实现。
 *
 * <p>购物车存放在 Session，以用户 id 作为 key 前缀：</p>
 * <pre>NAME_PREFIX + userId -> List&lt;Integer&gt; productIds</pre>
 *
 * <p>架构简化：购物车不持久化到数据库/Redis，仅适用于演示项目。</p>
 */
@Service
public class ShopCartServiceImpl implements ShopCartService {

    @Autowired
    private ProductService productService;

    /**
     * 加入购物车：将商品 id 写入 Session List 中。
     *
     * @throws Exception 未登录或其他错误
     */
    @Override
    public void addCart(int productId, HttpServletRequest request) throws Exception {
        User loginUser = getLoginUser(request);

        // 1) 获取该用户购物车列表，没有则创建
        List<Integer> productIds = getCartList(loginUser, request);

        // 2) 直接追加商品 id（允许重复，后续统计数量）
        productIds.add(productId);
    }

    /**
     * 从购物车移除指定商品（只移除一次，如果有多个相同商品则数量减 1）。
     */
    @Override
    public void remove(int productId, HttpServletRequest request) throws Exception {
        User loginUser = getLoginUser(request);
        List<Integer> productIds = getCartList(loginUser, request);

        // 使用迭代器安全删除，避免 ConcurrentModificationException
        Iterator<Integer> iterator = productIds.iterator();
        while (iterator.hasNext()) {
            if (productId == iterator.next()) {
                iterator.remove();
                break; // 只删除一次
            }
        }
    }

    /**
     * 查看购物车详情：将 productId List 聚合为 OrderItem 列表（包含 count/subTotal）。
     */
    @Override
    public List<OrderItem> listCart(HttpServletRequest request) throws Exception {
        User loginUser = getLoginUser(request);
        List<Integer> productIds = (List<Integer>) request.getSession().getAttribute(NAME_PREFIX + loginUser.getId());

        // key: productId value:OrderItem
        Map<Integer, OrderItem> productMap = new HashMap<>();
        if (productIds == null) {
            return Collections.emptyList();
        }

        // 遍历商品 id，聚合数量与小计
        for (Integer pid : productIds) {
            if (!productMap.containsKey(pid)) {
                Product product = productService.findById(pid);
                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setProductId(pid);
                item.setCount(1);
                item.setSubTotal(product.getShopPrice());
                productMap.put(pid, item);
            } else {
                // 已存在该商品：数量 +1，小计 + 单价
                OrderItem item = productMap.get(pid);
                item.setCount(item.getCount() + 1);
                item.setSubTotal(item.getSubTotal() + item.getProduct().getShopPrice());
            }
        }
        return new ArrayList<>(productMap.values());
    }

    /* ===================== private helper ===================== */

    private User getLoginUser(HttpServletRequest request) throws Exception {
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) {
            throw new Exception("未登录！请重新登录");
        }
        return loginUser;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> getCartList(User loginUser, HttpServletRequest request) {
        List<Integer> productIds = (List<Integer>) request.getSession().getAttribute(NAME_PREFIX + loginUser.getId());
        if (productIds == null) {
            productIds = new ArrayList<>();
            request.getSession().setAttribute(NAME_PREFIX + loginUser.getId(), productIds);
        }
        return productIds;
    }
}

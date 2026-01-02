package priv.jesse.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import priv.jesse.mall.dao.OrderDao;
import priv.jesse.mall.dao.OrderItemDao;
import priv.jesse.mall.dao.ProductDao;
import priv.jesse.mall.entity.Order;
import priv.jesse.mall.entity.OrderItem;
import priv.jesse.mall.entity.Product;
import priv.jesse.mall.entity.User;
import priv.jesse.mall.service.OrderService;
import priv.jesse.mall.service.ShopCartService;
import priv.jesse.mall.service.exception.LoginException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 订单业务实现。
 *
 * <p>本项目订单流程为简化版：</p>
 * <ol>
 *   <li>购物车数据来自 Session（{@link ShopCartService}）</li>
 *   <li>提交订单时生成 Order 与 OrderItem，并计算 total</li>
 *   <li>支付/收货仅做状态流转，不接入真实支付/物流系统</li>
 * </ol>
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private ShopCartService shopCartService;


    @Override
    public Order findById(int id) {
        // 按主键查询订单
        return orderDao.getOne(id);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        // 分页查询订单（后台订单管理使用）
        return orderDao.findAll(pageable);
    }

    @Override
    public List<Order> findAllExample(Example<Order> example) {
        // 按示例对象条件查询
        return orderDao.findAll(example);
    }

    @Override
    public void update(Order order) {
        // 更新订单
        orderDao.save(order);
    }

    @Override
    public int create(Order order) {
        // 创建订单并返回 id
        Order saved = orderDao.save(order);
        return saved.getId();
    }

    @Override
    public void delById(int id) {
        // 按主键删除订单
        orderDao.delete(id);
    }

    /**
     * 查询订单项详情。
     *
     * <p>注意：订单项表中只存 productId，这里需要回填 Product 对象，
     * 方便前台/后台展示商品信息。</p>
     */
    @Override
    public List<OrderItem> findItems(int orderId) {
        // 1) 查询订单项
        List<OrderItem> list = orderItemDao.findByOrderId(orderId);

        // 2) 回填商品信息（N+1 查询，演示项目可接受；生产建议 join 或批量查询）
        for (OrderItem orderItem : list) {
            Product product = productDao.findOne(orderItem.getProductId());
            orderItem.setProduct(product);
        }
        return list;
    }

    /**
     * 更改订单状态。
     *
     * @param id     订单 id
     * @param status 目标状态
     */
    @Override
    public void updateStatus(int id, int status) {
        // 1) 查询订单
        Order order = orderDao.findOne(id);
        // 2) 修改状态
        order.setState(status);
        // 3) 持久化
        orderDao.save(order);
    }

    /**
     * 查找当前登录用户的订单列表。
     *
     * <p>用户信息来自 Session（key=user）。若未登录则抛 LoginException。</p>
     */
    @Override
    public List<Order> findUserOrder(HttpServletRequest request) {
        // 1) 从 Session 获取登录用户
        Object user = request.getSession().getAttribute("user");
        if (user == null) {
            throw new LoginException("请登录！");
        }

        // 2) 按 userId 查询订单
        User loginUser = (User) user;
        return orderDao.findByUserId(loginUser.getId());
    }

    /**
     * 支付（模拟）。
     *
     * <p>本项目不实现真实支付，仅修改订单状态为待发货（STATE_WAITE_SEND）。</p>
     */
    @Override
    public void pay(int orderId) {
        // 1) 校验订单存在
        Order order = orderDao.findOne(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2) 直接更新状态（DAO 中的 updateState 可能是自定义 @Modifying 查询）
        orderDao.updateState(STATE_WAITE_SEND, order.getId());
    }

    /**
     * 提交订单。
     *
     * <p>事务说明：该方法标注 @Transactional，写入订单主表与订单项表应保证原子性。</p>
     * <p>流程：</p>
     * <ol>
     *   <li>校验登录态</li>
     *   <li>从购物车读取 OrderItem 列表</li>
     *   <li>先保存 Order 主表（获取 orderId）</li>
     *   <li>遍历保存 OrderItem 并累计 total</li>
     *   <li>回写 total 到 Order</li>
     *   <li>重定向到订单列表页</li>
     * </ol>
     */
    @Override
    @Transactional
    public void submit(String name, String phone, String addr,
                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 1) 校验登录
        Object user = request.getSession().getAttribute("user");
        if (user == null) {
            throw new LoginException("请登录！");
        }
        User loginUser = (User) user;

        // 2) 组装订单对象
        Order order = new Order();
        order.setName(name);
        order.setPhone(phone);
        order.setAddr(addr);
        order.setOrderTime(new Date());
        order.setUserId(loginUser.getId());
        order.setState(STATE_NO_PAY);

        // 3) 从购物车读取订单项（包含商品、数量、小计等）
        List<OrderItem> orderItems = shopCartService.listCart(request);

        // 4) 先保存订单主表，获取 orderId
        Double total = 0.0;
        order.setTotal(total); // 先写 0，后续回写
        order = orderDao.save(order);

        // 5) 保存订单项并累计总价
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            total += orderItem.getSubTotal();
            orderItemDao.save(orderItem);
        }

        // 6) 回写订单总价
        order.setTotal(total);
        orderDao.save(order);

        // 7) 提交后重定向到订单列表页面
        response.sendRedirect("/mall/order/toList.html");
    }

    /**
     * 确认收货（模拟）。
     */
    @Override
    public void receive(int orderId) {
        // 1) 校验订单存在
        Order order = orderDao.findOne(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2) 修改状态为完成
        orderDao.updateState(STATE_COMPLETE, order.getId());
    }
}

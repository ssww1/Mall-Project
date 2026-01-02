package priv.jesse.mall.web.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import priv.jesse.mall.entity.Order;
import priv.jesse.mall.entity.OrderItem;
import priv.jesse.mall.entity.pojo.ResultBean;
import priv.jesse.mall.service.OrderService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 前台订单模块：下单、订单列表、订单详情、支付（模拟）、确认收货。
 *
 * <p>说明：</p>
 * <ul>
 *   <li>订单相关接口大部分依赖登录态；未登录访问会被 {@link priv.jesse.mall.filter.AuthorizationFilter} 拦截重定向。</li>
 *   <li>支付逻辑未接入第三方支付，仅通过修改订单状态模拟支付完成。</li>
 * </ul>
 */
@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 打开订单列表页面（HTML）。
     */
    @RequestMapping("/toList.html")
    public String toOrderList() {
        // 返回 templates/mall/order/list.html
        return "mall/order/list";
    }

    /**
     * 查询当前登录用户的订单列表（JSON）。
     *
     * @param request 用于从 Session 获取登录用户（OrderService 内部会校验）
     */
    @RequestMapping("/list.do")
    @ResponseBody
    public ResultBean<List<Order>> listData(HttpServletRequest request) {
        // 由 service 负责根据 session userId 查询订单
        List<Order> orders = orderService.findUserOrder(request);
        return new ResultBean<>(orders);
    }

    /**
     * 查询订单详情（订单项列表，JSON）。
     *
     * @param orderId 订单 id
     */
    @RequestMapping("/getDetail.do")
    @ResponseBody
    public ResultBean<List<OrderItem>> getDetail(int orderId) {
        // 查询订单项，并在 service 内部回填商品信息（若实现了）
        List<OrderItem> orderItems = orderService.findItems(orderId);
        return new ResultBean<>(orderItems);
    }

    /**
     * 提交订单。
     *
     * <p>关键流程（在 OrderServiceImpl.submit 中实现）：</p>
     * <ol>
     *   <li>校验登录态（从 Session 读取 user）</li>
     *   <li>读取购物车（Session）生成订单与订单项</li>
     *   <li>计算订单总价 total</li>
     *   <li>保存订单主表与订单项表（事务）</li>
     *   <li>重定向到订单列表页</li>
     * </ol>
     */
    @RequestMapping("/submit.do")
    public void submit(String name,
                       String phone,
                       String addr,
                       HttpServletRequest request,
                       HttpServletResponse response) throws Exception {
        // 订单提交核心逻辑在 service 中
        orderService.submit(name, phone, addr, request, response);
    }

    /**
     * 支付方法（模拟）。
     *
     * <p>说明：项目中不接入真实支付，只修改订单状态为“待发货/已支付”。</p>
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ResultBean<Boolean> pay(int orderId, HttpServletResponse response) throws IOException {
        // 调用 service 修改订单状态
        orderService.pay(orderId);
        return new ResultBean<>(true);
    }

    /**
     * 确认收货（模拟）。
     *
     * <p>调用 service 更新订单状态为“已完成”。</p>
     */
    @RequestMapping("receive.do")
    @ResponseBody
    public ResultBean<Boolean> receive(int orderId, HttpServletResponse response) throws IOException {
        orderService.receive(orderId);
        return new ResultBean<>(true);
    }
}

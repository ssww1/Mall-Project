package priv.jesse.mall.web.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import priv.jesse.mall.entity.Order;
import priv.jesse.mall.entity.OrderItem;
import priv.jesse.mall.entity.pojo.ResultBean;
import priv.jesse.mall.service.OrderService;

import java.util.List;

/**
 * 后台管理：订单管理。
 *
 * <p>
 * 主要能力：
 * </p>
 * <ul>
 * <li>订单列表分页查询</li>
 * <li>订单详情（订单项）查询</li>
 * <li>发货：将订单状态更新为“已发货”（status=3）</li>
 * </ul>
 */
@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 打开订单列表页面。
     */
    @RequestMapping("/toList.html")
    public String toList() {
        // 返回 templates/admin/order/list.html
        return "admin/order/list";
    }

    /**
     * 获取所有订单的总数（用于分页）。
     */
    @ResponseBody
    @RequestMapping("/getTotal.do")
    public ResultBean<Integer> getTotal() {
        // 这里 pageable 的页码/大小不重要，只用于读取 totalElements
        Pageable pageable = new PageRequest(1, 15, null);
        int total = (int) orderService.findAll(pageable).getTotalElements();
        return new ResultBean<>(total);
    }

    /**
     * 分页获取订单列表。
     *
     * @param pageindex 页码，从 0 开始
     * @param pageSize  每页大小
     */
    @ResponseBody
    @RequestMapping("/list.do")
    public ResultBean<List<Order>> listData(int pageindex,
            @RequestParam(value = "pageSize", defaultValue = "15") int pageSize) {
        // 1) 构造分页条件
        Pageable pageable = new PageRequest(pageindex, pageSize, null);
        // 2) 查询当前页订单数据
        List<Order> list = orderService.findAll(pageable).getContent();
        // 3) 返回给前端
        return new ResultBean<>(list);
    }

    /**
     * 查询订单详情（订单项列表）。
     *
     * @param orderId 订单 id
     */
    @ResponseBody
    @RequestMapping("/getDetail.do")
    public ResultBean<List<OrderItem>> getDetail(int orderId) {
        // 由 service 负责将 OrderItem 与 Product 信息进行组装（如有）
        List<OrderItem> list = orderService.findItems(orderId);
        return new ResultBean<>(list);
    }

    /**
     * 发货（状态流转）。
     *
     * <p>
     * 本项目未实现真实物流/快递单号，仅通过修改状态模拟“已发货”。
     * </p>
     *
     * @param id 订单 id
     */
    @ResponseBody
    @RequestMapping("/send.do")
    public ResultBean<Boolean> send(int id) {
        // 发货：将订单状态更新为 3（该项目中约定 3=已发货）
        orderService.updateStatus(id, 3);
        return new ResultBean<>(true);
    }
}

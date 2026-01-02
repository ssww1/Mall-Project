package priv.jesse.mall.web.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import priv.jesse.mall.entity.User;
import priv.jesse.mall.entity.pojo.ResultBean;
import priv.jesse.mall.service.UserService;

import java.util.List;
import java.util.Map;

/**
 * 后台管理：用户管理（CRUD）。
 *
 * <p>
 * 功能：在后台分页查看用户列表、编辑用户资料、删除用户。
 * </p>
 * <p>
 * 说明：返回数据统一使用 {@link priv.jesse.mall.entity.pojo.ResultBean} 进行封装。
 * </p>
 */
@Controller
@RequestMapping("/admin/user")
public class AdminUserController {
    @Autowired
    private UserService userService;

    /**
     * 打开用户列表页面。
     */
    @RequestMapping("/toList.html")
    public String toList() {
        // 返回 templates/admin/user/list.html
        return "admin/user/list";
    }

    /**
     * 打开用户编辑页面。
     *
     * @param id  用户 id
     * @param map Model，用于向页面传递用户对象
     */
    @RequestMapping("/toEdit.html")
    public String toEdit(int id, Map<String, Object> map) {
        // 1) 根据用户 id 查询用户详情
        User user = userService.findById(id);
        // 2) 将用户对象放入 Model（map）供 Thymeleaf 页面渲染
        map.put("user", user);
        // 3) 返回编辑页面模板
        return "admin/user/edit";
    }

    /**
     * 分页获取用户列表（供后台列表页 Ajax 调用）。
     *
     * @param pageindex 页码，从 0 开始
     * @param pageSize  每页大小
     */
    @ResponseBody
    @RequestMapping("/list.do")
    public ResultBean<List<User>> findAllUser(
            int pageindex,
            @RequestParam(value = "pageSize", defaultValue = "15") int pageSize) {
        // 1) 构造分页对象
        Pageable pageable = new PageRequest(pageindex, pageSize, null);
        // 2) 查询分页结果，仅取当前页数据
        List<User> users = userService.findAll(pageable).getContent();
        // 3) 返回给前端
        return new ResultBean<>(users);
    }

    /**
     * 获取用户总数（用于分页组件）。
     */
    @ResponseBody
    @RequestMapping("/getTotal.do")
    public ResultBean<Integer> geTotal() {
        // 这里用任意 pageable 查询总数即可
        Pageable pageable = new PageRequest(1, 15, null);
        int total = (int) userService.findAll(pageable).getTotalElements();
        return new ResultBean<>(total);
    }

    /**
     * 删除用户。
     *
     * @param id 用户 id
     */
    @ResponseBody
    @RequestMapping("/del.do")
    public ResultBean<Boolean> del(int id) {
        // 直接按 id 删除
        userService.delById(id);
        return new ResultBean<>(true);
    }

    /**
     * 更新用户信息。
     *
     * <p>
     * 实现方式：先按 id 查询用户，再覆盖表单字段，最后 save。
     * </p>
     * <p>
     * 注意：此处没有做字段级别校验（如 email 格式、手机号长度），属于可扩展项。
     * </p>
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/update.do")
    public ResultBean<Boolean> update(int id,
            String username,
            String password,
            String name,
            String phone,
            String email,
            String addr) {
        // 1) 更新前先查询（避免直接 new 对象导致未填字段丢失）
        User user = userService.findById(id);

        // 2) 覆盖表单字段
        user.setId(id);
        user.setName(name);
        user.setUsername(username);
        user.setPassword(password);
        user.setAddr(addr);
        user.setEmail(email);
        user.setPhone(phone);

        // 3) 保存更新
        userService.update(user);

        return new ResultBean<>(true);
    }
}

package priv.jesse.mall.web.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import priv.jesse.mall.entity.User;
import priv.jesse.mall.entity.pojo.ResultBean;
import priv.jesse.mall.service.UserService;
import priv.jesse.mall.service.exception.LoginException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 前台用户模块：注册、登录、退出、用户名唯一性校验。
 *
 * <p>
 * 登录态通过 Session 保存：
 * </p>
 * <ul>
 * <li>key = "user"：当前登录用户</li>
 * </ul>
 *
 * <p>
 * 鉴权主要由 {@link priv.jesse.mall.filter.AuthorizationFilter} 完成：
 * 未登录访问受限页面会被重定向到登录页。
 * </p>
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 打开注册页面。
     *
     * @return Thymeleaf 模板：templates/mall/user/register.html
     */
    @RequestMapping("/toRegister.html")
    public String toRegister() {
        return "mall/user/register";
    }

    /**
     * 打开登录页面。
     *
     * @return Thymeleaf 模板：templates/mall/user/login.html
     */
    @RequestMapping("/toLogin.html")
    public String toLogin() {
        return "mall/user/login";
    }

    /**
     * 用户登录。
     *
     * <p>
     * 关键逻辑：
     * </p>
     * <ol>
     * <li>调用 {@link UserService#checkLogin(String, String)} 校验用户名/密码；</li>
     * <li>校验成功：将用户写入 Session（key=user）；</li>
     * <li>重定向到前台首页 /mall/index.html；</li>
     * <li>校验失败：抛出 {@link LoginException}，由全局异常处理转到错误页面。</li>
     * </ol>
     */
    @RequestMapping("/login.do")
    public void login(String username,
            String password,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        // 1) 校验用户名密码（实际查询数据库）
        User user = userService.checkLogin(username, password);

        if (user != null) {
            // 2) 登录成功：将用户对象写入 Session（后续鉴权依赖该字段）
            request.getSession().setAttribute("user", user);

            // 3) 重定向到首页（避免表单重复提交）
            response.sendRedirect("/mall/index.html");
        } else {
            // 4) 登录失败：抛异常，交由 GlobalExceptionHandler 统一处理
            throw new LoginException("登录失败！ 用户名或者密码错误");
        }

    }

    /**
     * 用户注册。
     *
     * <p>
     * 说明：此处直接保存用户信息到数据库，并未做密码加密与字段校验，属于演示实现。
     * </p>
     *
     * <p>
     * 流程：
     * </p>
     * <ol>
     * <li>将表单参数组装为 User 实体；</li>
     * <li>调用 userService.create(user) 保存到数据库；</li>
     * <li>注册成功后重定向到登录页面。</li>
     * </ol>
     */
    @RequestMapping("/register.do")
    public void register(String username,
            String password,
            String name,
            String phone,
            String email,
            String addr,
            HttpServletResponse response) throws IOException {
        // 1) 组装用户对象
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(password);
        user.setName(name);
        user.setEmail(email);
        user.setAddr(addr);

        // 2) 保存用户
        userService.create(user);

        // 3) 注册完成后重定向到登录页面
        response.sendRedirect("/mall/user/toLogin.html");
    }

    /**
     * 用户退出登录。
     *
     * <p>
     * 关键点：清理 Session 的 user 字段。
     * </p>
     */
    @RequestMapping("/logout.do")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1) 清理登录态
        request.getSession().removeAttribute("user");
        // 2) 退出后回到首页
        response.sendRedirect("/mall/index.html");
    }

    /**
     * 校验用户名是否唯一（供注册页 Ajax 调用）。
     *
     * @param username 用户名
     * @return data=true 表示可用；data=false 表示已存在
     */
    @ResponseBody
    @RequestMapping("/checkUsername.do")
    public ResultBean<Boolean> checkUsername(String username) {
        // 1) 根据 username 查询是否存在用户
        List<User> users = userService.findByUsername(username);

        // 2) 不存在则返回 true
        if (users == null || users.isEmpty()) {
            return new ResultBean<>(true);
        }

        // 3) 存在则返回 false
        return new ResultBean<>(false);
    }

    /**
     * 错误页面转发入口。
     *
     * <p>
     * 当发生异常时，全局异常处理器可能 forward 到该地址，最终返回 templates/error.html。
     * </p>
     */
    @RequestMapping("/error.html")
    public String error(HttpServletResponse response, HttpServletRequest request) {
        return "error";
    }
}

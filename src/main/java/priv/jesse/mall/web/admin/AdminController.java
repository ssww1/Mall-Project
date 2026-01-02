package priv.jesse.mall.web.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import priv.jesse.mall.entity.AdminUser;
import priv.jesse.mall.entity.pojo.ResultBean;
import priv.jesse.mall.service.AdminUserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 后台管理：管理员登录与后台首页入口。
 *
 * <p>
 * 说明：后台鉴权依赖 {@link priv.jesse.mall.filter.AuthorizationFilter}，
 * 登录成功后会在 Session 中写入 key=login_user 的管理员对象。
 * </p>
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminUserService adminUserService;

    /**
     * 打开后台管理首页（仪表盘）。
     *
     * <p>
     * 返回 Thymeleaf 模板：templates/admin/index.html
     * </p>
     */
    @RequestMapping("/toIndex.html")
    public String toIndex() {
        return "admin/index";
    }

    /**
     * 打开后台登录页面。
     *
     * <p>
     * 返回 Thymeleaf 模板：templates/admin/login.html
     * </p>
     */
    @RequestMapping("/toLogin.html")
    public String toLogin() {
        // Spring MVC 根据返回的字符串 "admin/login" 查找并渲染对应模板
        return "admin/login";
    }

    /**
     * 管理员登录。
     *
     * <p>
     * 流程：
     * </p>
     * <ol>
     * <li>调用
     * {@link AdminUserService#checkLogin(HttpServletRequest, String, String)}
     * 校验用户名密码；</li>
     * <li>校验成功后在 Session 写入 login_user；</li>
     * <li>重定向到后台首页。</li>
     * </ol>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/login.do")
    public void login(String username, String password, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 校验登录并写入 session（service 内部实现）
        adminUserService.checkLogin(request, username, password);
        // 登录成功后跳转后台首页
        response.sendRedirect("/mall/admin/toIndex.html");
    }

    /**
     * 退出登录
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/logout.do")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1) 清理后台登录态（由 AuthorizationFilter 依赖该 session key 判断是否已登录）
        request.getSession().removeAttribute("login_user");
        // 2) 重定向到后台登录页
        response.sendRedirect("toLogin.html");
    }
}

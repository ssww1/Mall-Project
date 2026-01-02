package priv.jesse.mall.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * 权限拦截器（基于 Session 的简易鉴权）。
 *
 * <p>拦截策略：</p>
 * <ul>
 *   <li>只拦截 *.do / *.html 请求（静态资源不拦截）</li>
 *   <li>对登录/注册/首页/商品浏览/图片等公共资源放行</li>
 *   <li>其余请求进入鉴权流程：
 *     <ul>
 *       <li>URL 含 admin：要求 Session 中存在 login_user</li>
 *       <li>否则：要求 Session 中存在 user</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>说明：该实现适用于演示项目；生产环境建议使用 Spring Security 并配置 CSRF、防暴力破解等能力。</p>
 */
@WebFilter
public class AuthorizationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        // 本项目无额外初始化逻辑
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 1) 设置跨域响应头（演示环境使用 *，生产应改为白名单）
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept,X-Custom-Header");
        response.setHeader("X-Powered-By", "SpringBoot");

        // 2) 预检请求直接返回空 JSON（避免浏览器跨域失败）
        if ("option".equalsIgnoreCase(request.getMethod())) {
            responseJSON(response, new HashMap<>());
            return;
        }

        // 3) 获取当前请求 URL
        String path = request.getRequestURL().toString();

        // 4) 只拦截 .do / .html（其余静态资源放行，如 js/css/font/image）
        if (path.endsWith(".do") || path.endsWith(".html")) {
            // 5) 放行白名单：登录注册页/登录注册接口/首页/商品相关/图片/h2-console 等
            if (path.endsWith("toLogin.html")
                    || path.endsWith("toRegister.html")
                    || path.endsWith("register.do")
                    || path.endsWith("login.do")
                    || path.endsWith("logout.do")
                    || path.endsWith("error.html")
                    || path.endsWith("checkUsername.do")
                    || path.contains("/mall/admin/product/img/")
                    || path.endsWith("index.html")
                    || path.endsWith("classification/list.do")
                    || path.contains("product")
                    || path.contains("/mall/h2-console")) {
                chain.doFilter(request, response);
            } else {
                // 6) 非白名单：进入鉴权逻辑
                processAccessControl(request, response, chain);
            }
        } else {
            // 7) 静态资源放行
            chain.doFilter(request, response);
        }
    }

    /**
     * 处理鉴权逻辑：根据 URL 判断是后台还是前台，然后检查对应 Session 登录态。
     */
    private void processAccessControl(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Object adminUser = request.getSession().getAttribute("login_user");
        Object user = request.getSession().getAttribute("user");
        String url = request.getRequestURL().toString();

        // 1) 后台 URL：要求 login_user
        if (url.contains("admin")) {
            if (adminUser == null) {
                // 未登录 → 重定向后台登录页
                response.sendRedirect("/mall/admin/toLogin.html");
            } else {
                // 已登录 → 放行
                chain.doFilter(request, response);
            }
        } else {
            // 2) 前台用户相关 URL：要求 user
            if (user == null) {
                response.sendRedirect("/mall/user/toLogin.html");
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    @Override
    public void destroy() {
        // 本项目无资源回收逻辑
    }

    /**
     * 输出 JSON 响应。
     *
     * @param response HttpServletResponse
     * @param object   任意可序列化对象
     */
    public static void responseJSON(HttpServletResponse response, Object object) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("UTF-8");

        if (object == null) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = mapper.writeValueAsString(object);

        OutputStream out = response.getOutputStream();
        out.write(jsonStr.getBytes("UTF-8"));
        out.flush();
    }
}

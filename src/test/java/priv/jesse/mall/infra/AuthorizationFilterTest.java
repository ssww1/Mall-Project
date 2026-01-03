package priv.jesse.mall.infra;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import priv.jesse.mall.filter.AuthorizationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.*;

/**
 * AuthorizationFilter 单元测试
 *
 * 目标：验证权限过滤器对不同 URL 的处理行为（放行/重定向）。
 *
 * 覆盖点：
 * 1) 访问后台资源且未登录（Session 无 login_user）→ 302 重定向到后台登录页
 * 2) 访问公开页面（如用户登录页）→ 直接放行到 chain.doFilter
 */
public class AuthorizationFilterTest {

    @Test
    public void doFilter_adminUrl_withoutLogin_redirectToAdminLogin() throws Exception {
        AuthorizationFilter filter = new AuthorizationFilter();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getMethod()).thenReturn("GET");
        when(req.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1:8081/mall/admin/product/toList.html"));
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("login_user")).thenReturn(null);

        filter.doFilter(req, res, chain);

        verify(res).sendRedirect("/mall/admin/toLogin.html");
        verify(chain, never()).doFilter(req, res);
    }

    @Test
    public void doFilter_publicLogin_shouldPass() throws Exception {
        AuthorizationFilter filter = new AuthorizationFilter();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getMethod()).thenReturn("GET");
        when(req.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1:8081/mall/user/toLogin.html"));

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(res, never()).sendRedirect(anyString());
    }
}


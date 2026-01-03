package priv.jesse.mall.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import priv.jesse.mall.entity.OrderItem;
import priv.jesse.mall.entity.Product;
import priv.jesse.mall.entity.User;
import priv.jesse.mall.service.impl.ShopCartServiceImpl;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * ShopCartServiceImpl 单元测试
 *
 * 购物车逻辑覆盖：
 * 1) 未登录异常
 * 2) 第一次添加商品 -> 购物车创建
 * 3) 重复添加同商品 -> count 累加
 * 4) 移除商品 -> 商品消失
 * 5) 购物车为空返回空 list
 */
@RunWith(MockitoJUnitRunner.class)
public class ShopCartServiceImplTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ShopCartServiceImpl cartService;

    private MockHttpServletRequest request;
    private HttpSession session;

    private static final int PID = 100;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        session = request.getSession();
        // mock product
        Product p = new Product();
        p.setId(PID);
        p.setShopPrice(99.0);
        Mockito.when(productService.findById(PID)).thenReturn(p);
    }

    @Test(expected = Exception.class)
    public void addCart_notLogin_throwException() throws Exception {
        cartService.addCart(PID, request);
    }

    @Test
    public void addCart_firstTime_createCartAndAdd() throws Exception {
        loginUser();
        cartService.addCart(PID, request);
        List<OrderItem> list = cartService.listCart(request);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(Integer.valueOf(1), list.get(0).getCount());
    }

    @Test
    public void addCart_duplicateItem_shouldIncreaseCount() throws Exception {
        loginUser();
        cartService.addCart(PID, request);
        cartService.addCart(PID, request);
        List<OrderItem> list = cartService.listCart(request);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(Integer.valueOf(2), list.get(0).getCount());
    }

    @Test
    public void removeItem_cartShouldBeEmpty() throws Exception {
        loginUser();
        cartService.addCart(PID, request);
        cartService.remove(PID, request);
        List<OrderItem> list = cartService.listCart(request);
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void listCart_noItem_returnEmptyList() throws Exception {
        loginUser();
        List<OrderItem> list = cartService.listCart(request);
        Assert.assertTrue(list.isEmpty());
    }

    private void loginUser() {
        User u = new User();
        u.setId(1);
        u.setUsername("tester");
        session.setAttribute("user", u);
    }
}

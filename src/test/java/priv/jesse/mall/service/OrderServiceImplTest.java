package priv.jesse.mall.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import priv.jesse.mall.dao.OrderDao;
import priv.jesse.mall.dao.OrderItemDao;
import priv.jesse.mall.dao.ProductDao;
import priv.jesse.mall.entity.Order;
import priv.jesse.mall.service.impl.OrderServiceImpl;

import static org.mockito.Mockito.*;

/**
 * OrderServiceImpl 单元测试
 *
 * <p>目的：覆盖 "支付" 业务分支，不启动 Spring 容器，使用 Mockito Mock Dao 依赖。</p>
 *
 * 断言点：
 * 1. pay() 传入合法订单 id → 调用 updateState 将状态置为待发货。
 * 2. pay() 传入不存在订单 id → 抛 RuntimeException。
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;
    @Mock
    private OrderItemDao orderItemDao;
    @Mock
    private ProductDao productDao;
    @Mock
    private ShopCartService shopCartService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order mockOrder;

    @Before
    public void setUp() {
        mockOrder = new Order();
        mockOrder.setId(1);
        mockOrder.setState(OrderServiceImpl.STATE_NO_PAY);
        Mockito.when(orderDao.findOne(1)).thenReturn(mockOrder);
    }

    @Test
    public void pay_validOrder_updatesStatus() {
        // when
        orderService.pay(1);
        // then
        verify(orderDao).findOne(1);
        verify(orderDao).updateState(OrderServiceImpl.STATE_WAITE_SEND, 1);
    }

    @Test(expected = RuntimeException.class)
    public void pay_invalidOrder_throwsException() {
        when(orderDao.findOne(anyInt())).thenReturn(null);
        orderService.pay(999);
    }
}


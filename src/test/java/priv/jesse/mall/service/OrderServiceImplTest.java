package priv.jesse.mall.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import priv.jesse.mall.dao.OrderDao;
import priv.jesse.mall.dao.OrderItemDao;
import priv.jesse.mall.dao.ProductDao;
import priv.jesse.mall.entity.Order;
import priv.jesse.mall.service.impl.OrderServiceImpl;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * 只验证纯粹业务分支，不依赖 Spring 容器。
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


package priv.jesse.mall.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import priv.jesse.mall.dao.UserDao;
import priv.jesse.mall.entity.User;
import priv.jesse.mall.service.impl.UserServiceImpl;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试
 *
 * 覆盖目标：
 * 1. checkLogin 成功/失败路径
 * 2. findByUsername 空/非空
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User demo;

    @Before
    public void setUp() {
        demo = new User();
        demo.setId(1);
        demo.setUsername("demo");
        demo.setPassword("123");
    }

    @Test
    public void checkLogin_success_shouldReturnUser() {
        when(userDao.findByUsernameAndPassword("demo", "123")).thenReturn(demo);
        User u = userService.checkLogin("demo", "123");
        assertNotNull(u);
        assertEquals(Integer.valueOf(1), u.getId());
    }

    @Test
    public void checkLogin_fail_shouldReturnNull() {
        when(userDao.findByUsernameAndPassword(anyString(), anyString())).thenReturn(null);
        User u = userService.checkLogin("demo", "wrong");
        assertNull(u);
    }

    @Test
    public void findByUsername_emptyList() {
        when(userDao.findByUsername("new"))
                .thenReturn(Collections.emptyList());
        List<User> list = userService.findByUsername("new");
        assertTrue(list.isEmpty());
    }

    @Test
    public void findByUsername_exist() {
        when(userDao.findByUsername("demo"))
                .thenReturn(Collections.singletonList(demo));
        List<User> list = userService.findByUsername("demo");
        assertEquals(1, list.size());
        assertEquals("demo", list.get(0).getUsername());
    }
}

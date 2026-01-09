package priv.jesse.mall.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
 * 1. checkLogin 成功/失败路径（使用 BCrypt 验证密码）
 * 2. findByUsername 空/非空
 *
 * 注意：checkLogin 现在使用 BCrypt 验证密码，需要：
 * - Mock findByUsername 返回包含用户的列表
 * - 设置用户的密码为 BCrypt 加密后的字符串
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User demo;
    private BCryptPasswordEncoder passwordEncoder;

    @Before
    public void setUp() {
        // 创建 BCrypt 编码器用于生成测试用的加密密码
        passwordEncoder = new BCryptPasswordEncoder();

        demo = new User();
        demo.setId(1);
        demo.setUsername("demo");
        // 设置密码为 BCrypt 加密后的 "123"
        demo.setPassword(passwordEncoder.encode("123"));
    }

    @Test
    public void checkLogin_success_shouldReturnUser() {
        // Mock findByUsername 返回包含用户的列表（新实现使用此方法）
        when(userDao.findByUsername("demo")).thenReturn(Collections.singletonList(demo));

        User u = userService.checkLogin("demo", "123");

        assertNotNull(u);
        assertEquals(Integer.valueOf(1), u.getId());
        assertEquals("demo", u.getUsername());
    }

    @Test
    public void checkLogin_fail_shouldReturnNull() {
        // 测试用户名不存在的情况
        when(userDao.findByUsername("demo")).thenReturn(Collections.emptyList());
        User u = userService.checkLogin("demo", "123");
        assertNull(u);

        // 测试密码错误的情况
        when(userDao.findByUsername("demo")).thenReturn(Collections.singletonList(demo));
        User u2 = userService.checkLogin("demo", "wrong");
        assertNull(u2);
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

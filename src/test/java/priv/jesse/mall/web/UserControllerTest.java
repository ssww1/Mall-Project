package priv.jesse.mall.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import priv.jesse.mall.entity.User;
import priv.jesse.mall.service.UserService;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 集成测试（MockMvc）
 *
 * 使用 MockMvc + MockBean 隔离 Service 依赖，验证：
 * 1. 登录成功场景的重定向及 Session 写入
 * 2. 用户名唯一性接口 JSON 返回值
 *
 * 说明：使用 @MockBean mock 掉 UserService，聚焦 Web 层行为。
 * 注意：测试环境已禁用 Druid WebStatFilter，避免 NullPointerException。
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    /******************** 登录 ***************************/
    @Test
    public void login_success_shouldRedirectAndWriteSession() throws Exception {
        User u = new User();
        u.setId(1);
        u.setUsername("demo");
        Mockito.when(userService.checkLogin("demo","123")).thenReturn(u);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/user/login.do")
                .param("username","demo")
                .param("password","123")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mall/index.html"));

        // 登录完成后 Session 中应有 "user"
        User saved = (User) session.getAttribute("user");
        org.junit.Assert.assertNotNull(saved);
        org.junit.Assert.assertEquals("demo", saved.getUsername());
    }

    /*********************** 用户名唯一性 *********************/

    @Test
    public void checkUsername_unique_shouldReturnTrue() throws Exception {
        Mockito.when(userService.findByUsername("newUser"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/checkUsername.do")
                .param("username","newUser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data", is(true)));
    }

    @Test
    public void checkUsername_exist_shouldReturnFalse() throws Exception {
        Mockito.when(userService.findByUsername("exist"))
                .thenReturn(Collections.singletonList(new User()));

        mockMvc.perform(get("/user/checkUsername.do")
                .param("username","exist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(false)));
    }
}


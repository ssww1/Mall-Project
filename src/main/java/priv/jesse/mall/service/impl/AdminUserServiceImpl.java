package priv.jesse.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import priv.jesse.mall.dao.AdminUserDao;
import priv.jesse.mall.entity.AdminUser;
import priv.jesse.mall.service.AdminUserService;
import priv.jesse.mall.service.exception.LoginException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 后台管理员业务实现。
 *
 * <p>
 * 负责管理员的 CRUD 以及后台登录校验。
 * </p>
 * <p>
 * 登录校验成功后，会在 Session 写入 {@code login_user}，
 * 该字段由 {@link priv.jesse.mall.filter.AuthorizationFilter} 用于后台鉴权。
 * </p>
 * <p>
 * 密码安全：
 * </p>
 * <ul>
 * <li>使用 BCrypt 加盐哈希存储密码（创建时自动加密）</li>
 * <li>登录时使用 BCrypt 验证密码（不进行明文比对）</li>
 * <li>BCrypt 自动处理盐值，每次加密结果不同，但可通过 matches 方法验证</li>
 * </ul>
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {
    @Autowired
    private AdminUserDao adminUserDao;

    /**
     * BCrypt 密码编码器（自动加盐）。
     * <p>
     * BCrypt 每次加密结果不同，但可通过 matches 方法验证原密码。
     * </p>
     */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AdminUser findById(int id) {
        // JPA getOne 返回的是延迟加载代理对象（需要事务/持久化上下文）
        return adminUserDao.getOne(id);
    }

    @Override
    public Page<AdminUser> findAll(Pageable pageable) {
        // 分页查询所有管理员
        return adminUserDao.findAll(pageable);
    }

    @Override
    public List<AdminUser> findAllExample(Example<AdminUser> example) {
        // 按示例对象条件查询（Spring Data JPA Example）
        return adminUserDao.findAll(example);
    }

    @Override
    public void update(AdminUser adminUser) {
        // 1) 如果更新了密码，且密码不是 BCrypt 格式（不以 $2a$ 开头），则进行加密
        // 注意：BCrypt 密文格式为 $2a$10$... 或 $2b$10$...
        String password = adminUser.getPassword();
        if (password != null && !password.isEmpty() && !password.startsWith("$2a$") && !password.startsWith("$2b$")) {
            // 密码是明文，需要加密
            String encodedPassword = passwordEncoder.encode(password);
            adminUser.setPassword(encodedPassword);
        }

        // 2) save 在 JPA 中同时承担 insert/update 语义（基于主键是否存在）
        adminUserDao.save(adminUser);
    }

    @Override
    public int create(AdminUser adminUser) {
        // 1) 创建管理员时对密码进行 BCrypt 加密（自动加盐）
        String rawPassword = adminUser.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        adminUser.setPassword(encodedPassword);

        // 2) 保存管理员并返回生成后的 id
        AdminUser saved = adminUserDao.save(adminUser);
        return saved.getId();
    }

    @Override
    public void delById(int id) {
        // 按主键删除
        adminUserDao.delete(id);
    }

    /**
     * 管理员登录校验（使用 BCrypt 验证密码）。
     *
     * <p>
     * 实现逻辑：
     * </p>
     * <ol>
     * <li>先根据用户名查询管理员（数据库中的密码已是 BCrypt 加密后的字符串）</li>
     * <li>使用 passwordEncoder.matches() 将用户输入的明文密码与数据库中的 BCrypt 密文进行比对</li>
     * <li>匹配成功：将管理员对象写入 Session（key=login_user），供 Filter 鉴权使用</li>
     * <li>匹配失败：抛出 LoginException，由全局异常处理器统一处理</li>
     * </ol>
     * <p>
     * 注意：BCrypt 会自动处理盐值，每次加密结果不同，但可通过 matches 验证。
     * </p>
     */
    @Override
    public AdminUser checkLogin(HttpServletRequest request, String username, String pwd) {
        // 1) 根据用户名查询管理员（注意：数据库中的 password 字段已是 BCrypt 加密后的字符串）
        AdminUser adminUser = adminUserDao.findByUsername(username);

        if (adminUser == null) {
            // 用户名不存在，抛出登录异常
            throw new LoginException("用户名或密码错误");
        }

        // 2) 使用 BCrypt 验证密码：matches(明文密码, BCrypt 密文)
        if (passwordEncoder.matches(pwd, adminUser.getPassword())) {
            // 3) 登录成功：写入 Session，供 Filter 鉴权
            request.getSession().setAttribute("login_user", adminUser);
            return adminUser;
        } else {
            // 4) 密码不匹配：抛出业务异常，由 GlobalExceptionHandler 统一处理
            throw new LoginException("用户名或密码错误");
        }
    }
}

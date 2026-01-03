package priv.jesse.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import priv.jesse.mall.dao.UserDao;
import priv.jesse.mall.entity.User;
import priv.jesse.mall.service.UserService;

import java.util.List;

/**
 * 用户服务实现。
 *
 * <p>
 * 提供用户的 CRUD、登录校验、用户名查重等基础功能。
 * </p>
 * <p>
 * 密码安全：
 * </p>
 * <ul>
 * <li>使用 BCrypt 加盐哈希存储密码（注册时自动加密）</li>
 * <li>登录时使用 BCrypt 验证密码（不进行明文比对）</li>
 * <li>BCrypt 自动处理盐值，每次加密结果不同，但可通过 matches 方法验证</li>
 * </ul>
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    /**
     * BCrypt 密码编码器（自动加盐）。
     * <p>
     * BCrypt 每次加密结果不同，但可通过 matches 方法验证原密码。
     * </p>
     */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findById(int id) {
        // 按主键查询用户
        return userDao.getOne(id);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        // 分页查询所有用户（后台管理使用）
        return userDao.findAll(pageable);
    }

    @Override
    public List<User> findAllExample(Example<User> example) {
        // 按示例对象条件查询
        return userDao.findAll(example);
    }

    @Override
    public void update(User user) {
        // 1) 如果更新了密码，且密码不是 BCrypt 格式（不以 $2a$ 开头），则进行加密
        // 注意：BCrypt 密文格式为 $2a$10$... 或 $2b$10$...
        String password = user.getPassword();
        if (password != null && !password.isEmpty() && !password.startsWith("$2a$") && !password.startsWith("$2b$")) {
            // 密码是明文，需要加密
            String encodedPassword = passwordEncoder.encode(password);
            user.setPassword(encodedPassword);
        }

        // 2) 保存用户（JPA save 同时支持新增/更新）
        userDao.save(user);
    }

    @Override
    public int create(User user) {
        // 1) 注册时对密码进行 BCrypt 加密（自动加盐）
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        // 2) 保存用户并返回主键
        return userDao.save(user).getId();
    }

    @Override
    public void delById(int id) {
        // 按主键删除用户
        userDao.delete(id);
    }

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名（未做模糊匹配）
     * @return 用户列表（用户名可能重复，但实际业务中应保证唯一）
     */
    @Override
    public List<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    /**
     * 用户登录校验（使用 BCrypt 验证密码）。
     *
     * <p>
     * 实现逻辑：
     * </p>
     * <ol>
     * <li>先根据用户名查询用户（数据库中的密码已是 BCrypt 加密后的字符串）</li>
     * <li>使用 passwordEncoder.matches() 将用户输入的明文密码与数据库中的 BCrypt 密文进行比对</li>
     * <li>匹配成功返回用户对象，否则返回 null</li>
     * </ol>
     * <p>
     * 注意：BCrypt 会自动处理盐值，每次加密结果不同，但可通过 matches 验证。
     * </p>
     */
    @Override
    public User checkLogin(String username, String password) {
        // 1) 根据用户名查询用户（注意：数据库中的 password 字段已是 BCrypt 加密后的字符串）
        List<User> users = userDao.findByUsername(username);
        if (users == null || users.isEmpty()) {
            return null;
        }

        // 2) 取第一个用户（业务上用户名应唯一，但当前实现返回 List）
        User user = users.get(0);

        // 3) 使用 BCrypt 验证密码：matches(明文密码, BCrypt 密文)
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        // 4) 密码不匹配返回 null
        return null;
    }
}

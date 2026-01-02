package priv.jesse.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import priv.jesse.mall.dao.UserDao;
import priv.jesse.mall.entity.User;
import priv.jesse.mall.service.UserService;

import java.util.List;

/**
 * 用户服务实现。
 *
 * <p>提供用户的 CRUD、登录校验、用户名查重等基础功能。</p>
 * <p>注意：当前实现中密码为明文存储，生产环境应加密（如 BCrypt）。</p>
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

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
        // 保存用户（JPA save 同时支持新增/更新）
        userDao.save(user);
    }

    @Override
    public int create(User user) {
        // 创建用户并返回主键
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
     * 用户登录校验。
     *
     * <p>说明：当前为简单实现，仅匹配用户名和密码。</p>
     * <p>安全建议：</p>
     * <ul>
     *   <li>密码应加盐哈希存储（如 BCrypt）</li>
     *   <li>增加登录失败次数限制</li>
     *   <li>考虑使用 Spring Security 等安全框架</li>
     * </ul>
     */
    @Override
    public User checkLogin(String username, String password) {
        // 直接查询匹配用户名和密码的用户（明文密码匹配）
        return userDao.findByUsernameAndPassword(username, password);
    }
}

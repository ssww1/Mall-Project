package priv.jesse.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * <p>负责管理员的 CRUD 以及后台登录校验。</p>
 * <p>登录校验成功后，会在 Session 写入 {@code login_user}，
 * 该字段由 {@link priv.jesse.mall.filter.AuthorizationFilter} 用于后台鉴权。</p>
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {
    @Autowired
    private AdminUserDao adminUserDao;

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
        // save 在 JPA 中同时承担 insert/update 语义（基于主键是否存在）
        adminUserDao.save(adminUser);
    }

    @Override
    public int create(AdminUser adminUser) {
        // 创建管理员
        AdminUser saved = adminUserDao.save(adminUser);
        // 返回生成后的 id
        return saved.getId();
    }

    @Override
    public void delById(int id) {
        // 按主键删除
        adminUserDao.delete(id);
    }

    @Override
    public AdminUser checkLogin(HttpServletRequest request, String username, String pwd) {
        // 1) 根据用户名密码查询管理员（演示项目：明文密码匹配）
        AdminUser adminUser = adminUserDao.findByUsernameAndPassword(username, pwd);

        if (adminUser != null) {
            // 2) 登录成功：写入 Session，供 Filter 鉴权
            request.getSession().setAttribute("login_user", adminUser);
        } else {
            // 3) 登录失败：抛出业务异常，由 GlobalExceptionHandler 统一处理
            throw new LoginException("用户名或密码错误");
        }
        return adminUser;
    }
}

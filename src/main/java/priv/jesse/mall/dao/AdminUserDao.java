package priv.jesse.mall.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import priv.jesse.mall.entity.AdminUser;

public interface AdminUserDao extends JpaRepository<AdminUser, Integer> {
    AdminUser findByUsernameAndPassword(String username, String pwd);

    /**
     * 根据用户名查询管理员（用于 BCrypt 密码验证）。
     *
     * @param username 用户名
     * @return 管理员对象，如果不存在返回 null
     */
    AdminUser findByUsername(String username);
}

package priv.jesse.mall.web.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 前台首页入口 Controller。
 *
 * <p>仅负责页面跳转，无复杂业务逻辑。</p>
 */
@Controller
public class IndexController {

    /**
     * 打开前台首页。
     *
     * @return templates/mall/index.html
     */
    @RequestMapping("/index.html")
    public String toIndex() {
        return "mall/index";
    }

    /**
     * 访问根路径时转发到首页。
     *
     * <p>使用 forward（服务器端转发）而不是 redirect（客户端重定向），
     * 这样浏览器地址栏仍保持根路径或按需要显示 index.html。</p>
     */
    @RequestMapping("/")
    public String index() {
        return "forward:/index.html";
    }

}

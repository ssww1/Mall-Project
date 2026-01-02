package priv.jesse.mall.web.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 前台分类模块 Controller。
 *
 * <p>注意：该 Controller 当前为空实现，所有与分类相关的查询逻辑
 * （如获取二级分类列表、按分类查询商品）均实现在 {@link ProductController} 中。
 * 后续可将相关方法重构至此。</p>
 */
@Controller
@RequestMapping("/classification")
public class ClassificationController {
}

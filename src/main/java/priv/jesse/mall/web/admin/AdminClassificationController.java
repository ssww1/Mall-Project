package priv.jesse.mall.web.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import priv.jesse.mall.entity.Classification;
import priv.jesse.mall.entity.pojo.ResultBean;
import priv.jesse.mall.service.ClassificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 后台管理：分类管理（一级分类/二级分类）。
 *
 * <p>
 * 分类实体为 {@link priv.jesse.mall.entity.Classification}：
 * </p>
 * <ul>
 * <li>type=1：一级分类（parentId 通常为 0 或 null）</li>
 * <li>type=2：二级分类（parentId 指向所属一级分类 id）</li>
 * </ul>
 *
 * <p>
 * 本 Controller 通过请求参数 type 来决定跳转到不同的后台模板页面。
 * </p>
 */
@Controller
@RequestMapping("/admin/classification")
public class AdminClassificationController {
    @Autowired
    private ClassificationService classificationService;

    /**
     * 打开一级/二级分类列表页面。
     *
     * @param type 1=一级分类，2=二级分类
     */
    @RequestMapping("/toList.html")
    public String toList(int type) {
        // 根据 type 返回不同模板
        if (type == 1) {
            return "admin/category/list";
        } else if (type == 2) {
            return "admin/categorysec/list";
        } else {
            return ""; // 异常路径，可扩展为错误页
        }
    }

    /**
     * 打开一级/二级分类新增页面。
     *
     * @param type 1=一级分类，2=二级分类
     */
    @RequestMapping("/toAdd.html")
    public String toAdd(int type) {
        if (type == 1) {
            return "admin/category/add";
        } else if (type == 2) {
            return "admin/categorysec/add";
        } else {
            return "";
        }
    }

    /**
     * 打开一级/二级分类编辑页面。
     *
     * @param id   要编辑的分类 id
     * @param type 分类类型
     * @param map  Model
     */
    @RequestMapping("/toEdit.html")
    public String toEdit(int id, int type, Map<String, Object> map) {
        // 1) 查询当前分类信息
        Classification classification = classificationService.findById(id);
        map.put("cate", classification);

        // 2) 根据类型返回不同模板
        if (type == 1) {
            // 一级分类编辑页
            return "admin/category/edit";
        } else if (type == 2) {
            // 二级分类编辑页，额外需要查询其所属的一级分类信息（用于页面回显）
            Classification parentClassification = classificationService.findById(classification.getParentId());
            map.put("cate", parentClassification);
            map.put("catese", classification);
            return "admin/categorysec/edit";
        } else {
            return "";
        }
    }

    /**
     * 新增分类。
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/add.do")
    public ResultBean<Boolean> add(String cname, int parentId, int type) {
        Classification classification = new Classification();
        classification.setCname(cname);
        classification.setParentId(parentId);
        classification.setType(type);
        classificationService.create(classification);
        return new ResultBean<>(true);
    }

    /**
     * 更新分类。
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/update.do")
    public ResultBean<Boolean> update(int id, String cname, int parentId, int type) {
        Classification classification = classificationService.findById(id);
        classification.setCname(cname);
        classification.setParentId(parentId);
        classification.setType(type);
        classificationService.update(classification);
        return new ResultBean<>(true);
    }

    /**
     * 删除分类。
     */
    @ResponseBody
    @RequestMapping("/del.do")
    public ResultBean<Boolean> del(int id) {
        classificationService.delById(id);
        return new ResultBean<>(true);
    }

    /**
     * 查询分类列表（分页或全量）。
     *
     * @param type      分类类型
     * @param pageindex 页码，-1 表示查询所有
     * @param pageSize  每页大小
     */
    @RequestMapping("/list.do")
    @ResponseBody
    public ResultBean<List<Classification>> findAll(int type,
            int pageindex, @RequestParam(value = "pageSize", defaultValue = "15") int pageSize) {
        List<Classification> list;
        // 特殊约定：pageindex = -1 时，查询该类型下所有分类（不分页）
        // 主要用于商品编辑页的“二级分类”下拉框
        if (pageindex == -1) {
            list = classificationService.findAll(type);
        } else {
            // 正常分页查询
            Pageable pageable = new PageRequest(pageindex, pageSize, null);
            list = classificationService.findAll(type, pageable).getContent();
        }
        return new ResultBean<>(list);
    }

    /**
     * 获取指定类型分类的总数。
     */
    @ResponseBody
    @RequestMapping("/getTotal.do")
    public ResultBean<Integer> getTotal(int type) {
        Pageable pageable = new PageRequest(1, 15, null);
        int count = (int) classificationService.findAll(type, pageable).getTotalElements();
        return new ResultBean<>(count);
    }
}

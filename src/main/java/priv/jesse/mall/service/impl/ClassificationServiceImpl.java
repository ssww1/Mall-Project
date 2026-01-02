package priv.jesse.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import priv.jesse.mall.dao.ClassificationDao;
import priv.jesse.mall.entity.Classification;
import priv.jesse.mall.service.ClassificationService;

import java.util.List;

/**
 * 商品分类服务实现。
 *
 * <p>管理一级分类和二级分类的 CRUD 操作。</p>
 * <p>分类类型：</p>
 * <ul>
 *   <li>type=1：一级分类（parentId 通常为 0 或 null）</li>
 *   <li>type=2：二级分类（parentId 指向所属一级分类 id）</li>
 * </ul>
 */
@Service
public class ClassificationServiceImpl implements ClassificationService {
    @Autowired
    private ClassificationDao classificationDao;

    @Override
    public Classification findById(int id) {
        // 根据主键查询分类
        return classificationDao.getOne(id);
    }

    @Override
    public List<Classification> findAll(int type) {
        // 查询指定类型的所有分类（不分页）
        return classificationDao.findByType(type);
    }

    /**
     * 分页查询指定类型的分类。
     *
     * @param type 分类类型（1=一级分类，2=二级分类）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Page<Classification> findAll(int type, Pageable pageable) {
        return classificationDao.findByType(type, pageable);
    }

    @Override
    public List<Classification> findAllExample(Example<Classification> example) {
        // 按示例对象条件查询
        return classificationDao.findAll(example);
    }

    @Override
    public void update(Classification classification) {
        // 更新分类信息
        classificationDao.save(classification);
    }

    @Override
    public int create(Classification classification) {
        // 创建分类并返回生成的主键
        Classification saved = classificationDao.save(classification);
        return saved.getId();
    }

    @Override
    public void delById(int id) {
        // 根据主键删除分类
        classificationDao.delete(id);
    }

    /**
     * 查询指定一级分类下的所有二级分类。
     *
     * @param cid 一级分类 id
     * @return 二级分类列表
     */
    @Override
    public List<Classification> findByParentId(int cid) {
        // 查询 parentId 等于指定 cid 的所有分类
        return classificationDao.findByParentId(cid);
    }
}

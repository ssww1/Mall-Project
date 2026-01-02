package priv.jesse.mall.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import priv.jesse.mall.dao.ClassificationDao;
import priv.jesse.mall.dao.ProductDao;
import priv.jesse.mall.entity.Classification;
import priv.jesse.mall.entity.Product;
import priv.jesse.mall.service.ProductService;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品服务实现。
 *
 * <p>提供商品的 CRUD、热门/最新商品查询、按分类查询等能力。</p>
 * <p>注意：本项目的“一级分类商品查询”逻辑为：</p>
 * <ol>
 *   <li>先根据一级分类 id 查询其下所有二级分类</li>
 *   <li>再根据二级分类 id 列表查询商品（IN 查询）</li>
 * </ol>
 */
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductDao productDao;
    @Autowired
    private ClassificationDao classificationDao;

    @Override
    public Product findById(int id) {
        // 按主键查询商品
        return productDao.getOne(id);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        // 分页查询所有商品（后台列表页使用）
        return productDao.findAll(pageable);
    }

    /**
     * 查找热门商品。
     *
     * <p>热门商品由 isHot=1 标识。</p>
     */
    @Override
    public List<Product> findHotProduct() {
        // pageable 传 null 表示不分页（取全部）
        return productDao.findByIsHot(1, null);
    }

    /**
     * 查找最新商品。
     *
     * <p>当前实现直接调用 productDao.findNew(pageable)，
     * 排序/时间窗口由 DAO 层实现（或数据库默认）。</p>
     */
    @Override
    public List<Product> findNewProduct(Pageable pageable) {
        // 注：曾计划“查找两周内上架的商品”，但现在逻辑简化为“按时间倒序分页取最新”
        return productDao.findNew(pageable);
    }

    /**
     * 根据一级分类查找商品。
     *
     * <p>实现策略：</p>
     * <ul>
     *   <li>先查出该一级分类下的所有二级分类</li>
     *   <li>收集二级分类 id 列表</li>
     *   <li>使用 csid IN (...) 查询商品</li>
     * </ul>
     */
    @Override
    public List<Product> findByCid(int cid, Pageable pageable) {
        // 1) 查找出所有二级分类
        List<Classification> sec = classificationDao.findByParentId(cid);

        // 2) 提取二级分类 id
        List<Integer> secIds = new ArrayList<>();
        for (Classification classification : sec) {
            secIds.add(classification.getId());
        }

        // 3) 根据二级分类 id 列表分页查询商品
        return productDao.findByCsidIn(secIds, pageable);
    }

    /**
     * 根据二级分类查找商品。
     */
    @Override
    public List<Product> findByCsid(int csid, Pageable pageable) {
        return productDao.findByCsid(csid, pageable);
    }


    @Override
    public void update(Product product) {
        // 保存商品：JPA save 同时支持新增/更新
        productDao.save(product);
    }

    @Override
    public int create(Product product) {
        // 新增商品并返回主键
        return productDao.save(product).getId();
    }

    @Override
    public void delById(int id) {
        // 按主键删除商品
        productDao.delete(id);
    }
}

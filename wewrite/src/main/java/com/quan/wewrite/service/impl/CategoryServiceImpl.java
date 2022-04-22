package com.quan.wewrite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.quan.wewrite.dao.mapper.ArticleMapper;
import com.quan.wewrite.dao.mapper.CategoryMapper;
import com.quan.wewrite.dao.pojo.Article;
import com.quan.wewrite.dao.pojo.Category;
import com.quan.wewrite.service.CategoryService;
import com.quan.wewrite.vo.CategoryVo;
import com.quan.wewrite.vo.ErrorCode;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.CategoryParam;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Override
    public CategoryVo findCategoryById(Long id){
        Category category = categoryMapper.selectById(id);
        CategoryVo categoryVo = new CategoryVo();
        BeanUtils.copyProperties(category,categoryVo);
        categoryVo.setId(String.valueOf(category.getId()));
        return categoryVo;
    }


    @Override
    public Result findAll() {
        List<Category> categories = this.categoryMapper.selectList(new LambdaQueryWrapper<>());
        //页面交互的对象
        return Result.success(copyList(categories));
    }

    @Override
    public Result findAllDetail() {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<>());
        //页面交互的对象
        return Result.success(copyList(categories));
    }

    @Override
    public Result categoriesDetailById(Long id) {
        Category category = categoryMapper.selectById(id);
        CategoryVo categoryVo = copy(category);
        return Result.success(categoryVo);
    }

    @Override
    public Result modifyCategory(CategoryParam categoryParam) {
        Category category = new Category();
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getId,categoryParam.getId());
        if(categoryMapper.selectCount(queryWrapper) == 0){
            return Result.fail(ErrorCode.CATEGORY_NOT_EXIST.getCode(),ErrorCode.CATEGORY_NOT_EXIST.getMsg());
        }
        category.setId(Long.parseLong(categoryParam.getId()));
        category.setCategoryName(categoryParam.getCategoryName());
        category.setAvatar(categoryParam.getAvatar());
        category.setDescription(categoryParam.getDescription());
        categoryMapper.updateById(category);
        //把类别缓存删掉
        Set<String> keys = redisTemplate.keys("Categories_" + "*");
        redisTemplate.delete(keys);
        return Result.success("修改类别成功");
    }

    @Override
    public Result deleteCategory(String id) {
        if(id.equals("1")) return Result.fail(-1,"系统错误");
        int delete = categoryMapper.deleteById(id);
        if(delete > 0){
            Article article = new Article();
            article.setCategoryId(1L);
            LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Article::getCategoryId,Long.parseLong(id));
            articleMapper.update(article,updateWrapper);

            //删除类别缓存
            Set<String> keys = redisTemplate.keys("Categories_" + "*");
            //删除缓存失败了
            if(redisTemplate.delete(keys) <= 0) {
                rocketMQTemplate.convertAndSend("blog-deleteArticle-failed",keys);
            }
        }

        return Result.success("删除类别成功");
    }

    @Override
    @Transactional
    public Result addCategory(CategoryParam categoryParam) {
        Category category = new Category();
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getCategoryName,categoryParam.getCategoryName());
        if(categoryMapper.selectCount(queryWrapper) != 0){
            return Result.fail(ErrorCode.CATEGORY_NAME_DUPLICATED.getCode(),ErrorCode.CATEGORY_NAME_DUPLICATED.getMsg());
        }
        category.setCategoryName(categoryParam.getCategoryName());
        category.setAvatar(categoryParam.getAvatar());
        category.setDescription(categoryParam.getDescription());
        int insert = categoryMapper.insert(category);
        //把类别缓存删掉
        Set<String> keys = redisTemplate.keys("Categories_" + "*");
        redisTemplate.delete(keys);
        return Result.success("创建类别成功");
    }


    public CategoryVo copy(Category category){
        CategoryVo categoryVo = new CategoryVo();
        BeanUtils.copyProperties(category,categoryVo);
        categoryVo.setId(String.valueOf(category.getId()));
        return categoryVo;
    }
    public List<CategoryVo> copyList(List<Category> categoryList){
        List<CategoryVo> categoryVoList = new ArrayList<>();
        for (Category category : categoryList) {
            categoryVoList.add(copy(category));
        }
        return categoryVoList;
    }

}

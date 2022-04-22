package com.quan.wewrite.controller;

import com.quan.wewrite.common.aop.LogAnnotation;
import com.quan.wewrite.common.cache.Cache;
import com.quan.wewrite.service.CategoryService;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.CategoryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("categorys")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 所有文章分类
     *
     * @return
     */
    @GetMapping
    @Cache(name = "Categories_all")
    @LogAnnotation(module = "文章分类",operation = "获取所有文章分类")
    public Result listCategory() {
        return categoryService.findAll();
    }

    /**
     * 导航-文章分类
     *
     * @return
     */
    @GetMapping("detail")
    @Cache(name = "Categories_nav")
    @LogAnnotation(module = "文章分类",operation = "获取 导航-文章分类")
    public Result categoriesDetail() {
        return categoryService.findAllDetail();
    }

    /**
     * 文章分类详情
     * @param id
     * @return
     */
    @GetMapping("detail/{id}")
    @Cache(name = "Categories_articles")
    @LogAnnotation(module = "文章分类",operation = "文章分类详情")
    public Result categoriesDetailById(@PathVariable("id") Long id){
        return categoryService.categoriesDetailById(id);
    }
    /**
     *  新建类别
     */
    @PostMapping("add")
    @LogAnnotation(module = "文章分类",operation = "创建文章分类")
    public Result addCategory(@RequestBody CategoryParam categoryParam){
        return categoryService.addCategory(categoryParam);
    }
    /**
     * 删除类别
     */
    @PostMapping("delete/{id}")
    public Result deleteCategory(@PathVariable("id") String id){
        return categoryService.deleteCategory(id);
    }

    /**
     * 修改类别
     * @param categoryParam
     * @return
     */
    @PostMapping("modify")
    public Result modifyCategory(@RequestBody CategoryParam categoryParam){
        return categoryService.modifyCategory(categoryParam);
    }

}

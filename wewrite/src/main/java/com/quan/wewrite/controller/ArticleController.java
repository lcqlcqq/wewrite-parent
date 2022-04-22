package com.quan.wewrite.controller;

import com.quan.wewrite.common.aop.LogAnnotation;
import com.quan.wewrite.common.cache.Cache;
import com.quan.wewrite.service.ArticleService;
import com.quan.wewrite.vo.ArticleVo;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.ArticleParam;
import com.quan.wewrite.vo.params.FavoritesParam;
import com.quan.wewrite.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("articles")
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    /**
     * 首页 文章列表
     * @param pageParams
     * @return
     */
    @PostMapping
    @LogAnnotation(module = "文章",operation = "获取首页文章列表")  //加上自定义注解，代表对接口记录日志
    @Cache(expire = 5 * 60 * 1000,name = "article_lists")
    public Result listArticles(@RequestBody PageParams pageParams) {
        return articleService.listArticles(pageParams);
    }

    /**
     * 首页 最热文章
     * @return
     */
    @PostMapping("hot")
    @Cache(expire = 3 * 60 * 1000,name = "article_hot")
    @LogAnnotation(module = "文章",operation = "获取最热文章列表")
    public Result hotArticles() {
        int limit = 5;
        return articleService.hotArticles(limit);
    }

    /**
     * 首页 最新文章
     * @return
     */
    @PostMapping("new")
    @Cache(expire = 90 * 1000,name = "article_new")
    @LogAnnotation(module = "文章",operation = "获取最新文章列表")
    public Result newArticles(){
        int limit = 5;
        return articleService.newArticles(limit);
    }
    /**
     * 首页 文章归档
     * @return
     */
    @PostMapping("listArchives")
    @Cache(expire = 2 * 60 * 1000,name = "article_arch")
    @LogAnnotation(module = "文章",operation = "获取归档文章列表")
    public Result listArchives(){
        return articleService.listArchives();
    }

    /**
     * 文章详情
     * @param id
     * @return
     */
    @PostMapping("view/{id}")
    @LogAnnotation(module = "文章",operation = "查看文章详情")
    public Result findArticleById(@PathVariable("id") Long id) {  //获取到路径{}里的参数
        ArticleVo articleVo = articleService.findArticleById(id);
        return Result.success(articleVo);
    }

    /**
     * 发布文章
     * @param articleParam
     * @return
     */
    @PostMapping("publish")
    @LogAnnotation(module = "文章",operation = "发布新文章")
    public Result publish(@RequestBody ArticleParam articleParam){
        return articleService.publish(articleParam);
    }

    /**
     * 根据文章id查询文章
     * @param id
     * @return
     */
    @PostMapping("{id}")
    @LogAnnotation(module = "文章",operation = "开始修改文章")
    public Result update(@PathVariable("id") Long id){
        ArticleVo articleVo = articleService.findArticleById(id);
        return Result.success(articleVo);
    }

    /**
     * 删除对应文章
     * @param id
     * @return
     */
    @PostMapping("del/{id}")
    @LogAnnotation(module = "文章",operation = "删除文章")
    public Result delete(@PathVariable("id")Long id){
        return articleService.delete(id);
    }

    /**
     * 文章置顶
     */
    @PostMapping("top/{id}")
    @LogAnnotation(module = "文章",operation = "置顶文章")
    public Result setTop(@PathVariable("id") Long id){
        return articleService.setTop(id);
    }
    /**
     * 查询收藏夹文章列表
     */
    @GetMapping("fav/{id}")
    @LogAnnotation(module = "文章",operation = "收藏夹")
    public Result favorites(@PathVariable("id") String id){
        return articleService.getFavoritesArticle(id);
    }

    /**
     * 查询某篇文章是否被收藏
     * @return
     */
    @PostMapping("fav/query")
    @LogAnnotation(module = "文章",operation = "查看文章是否被收藏")
    public Result getIsFavorites(@RequestBody FavoritesParam favoritesParam){
        System.out.println("文章id："+favoritesParam.getArticleId());
        return articleService.getIsFavorites(favoritesParam);
    }
    /**
     * 收藏文章
     */
    @PostMapping("fav/add")
    @LogAnnotation(module = "文章",operation = "收藏文章")
    public Result insertFavorites(@RequestBody FavoritesParam favoritesParam){
        return articleService.addFavoritesArticle(favoritesParam);
    }
    /**
     * 取消收藏文章
     */
    @PostMapping("fav/del")
    @LogAnnotation(module = "文章",operation = "收藏文章")
    public Result delFavorites(@RequestBody FavoritesParam favoritesParam){
        return articleService.delFavoritesArticle(favoritesParam);
    }
}

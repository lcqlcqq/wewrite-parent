package com.quan.wewrite.service;

import com.quan.wewrite.vo.ArticleVo;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.ArticleParam;
import com.quan.wewrite.vo.params.PageParams;

public interface ArticleService {
    /**
     * 分页查询 文章列表
     * @param pageParams
     * @return
     */
    Result listArticles(PageParams pageParams);

    /**
     * 最热文章
     * @param limit
     * @return
     */
    Result hotArticles(int limit);

    /**
     * 最新文章
     * @param limit
     * @return
     */
    Result newArticles(int limit);

    /**
     * 文章归档
     * @return
     */
    Result listArchives();

    /**
     * 查看文章详情
     * @param id
     * @return
     */
    ArticleVo findArticleById(Long id);

    /**
     * 发布文章
     * @param articleParam
     * @return
     */
    Result publish(ArticleParam articleParam);

    /**
     * 删除对应文章
     * @param id
     * @return
     */
    Result delete(Long id);

    /**
     * 置顶
     * @param id
     * @return
     */
    Result setTop(Long id);

    /**
     * 查询收藏夹文章列表
     * @param userId
     * @return
     */
    Result getFavoritesArticle(String userId);
}

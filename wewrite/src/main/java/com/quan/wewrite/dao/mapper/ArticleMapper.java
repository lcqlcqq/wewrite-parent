package com.quan.wewrite.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quan.wewrite.dao.dos.Archives;
import com.quan.wewrite.dao.pojo.Article;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleMapper extends BaseMapper<Article> {
    /**
     * 查询时间归档
     * @return
     */
    List<Archives> listArchives();

    /**
     * 自定义sql，分页查询
     * @param page
     * @param categoryId
     * @param tagId
     * @param year
     * @param month
     * @return
     */
    IPage<Article> listArticle(Page<Article> page,Long categoryId,Long tagId,String year,String month);

    /**
     * 查询用户收藏夹的文章
     * @param userId
     * @return
     */
    @Select("SELECT * FROM ms_article where ms_article.id in " +
            "(SELECT ms_favorites.article_id FROM ms_favorites where ms_favorites.user_id = #{userId})")
    List<Article> listFavorites(@Param("userId") Long userId);
}

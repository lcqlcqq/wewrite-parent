package com.quan.wewrite.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quan.wewrite.dao.pojo.Tag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TagMapper extends BaseMapper<Tag> {
    /**
     * 根据文章id查询标签列表
     * @param articleId
     * @return
     */
    List<Tag> findTagsByArticleId(Long articleId);

    /**
     * 查询最热的前limit条标签
     * @param limit
     * @return
     */
    List<Long> findHotsTagIds(int limit);

    /**
     * 根据id列表查询标签列表
     * @param tagIds
     * @return
     */
    List<Tag> findTagsByIds(List<Long> tagIds);

    /**
     * 删除文章-标签关联表的记录
     * @param tagId
     * @return
     */
    @Delete("delete from ms_article_tag where tag_id = #{tagId}")
    Integer deleteTagRecord(@Param("tagId") Long tagId);
}

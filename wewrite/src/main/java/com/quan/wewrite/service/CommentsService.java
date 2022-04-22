package com.quan.wewrite.service;

import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.CommentParam;

public interface CommentsService {
    /**
     * 根据文章id查询评论列表
     * @param articleId
     * @return
     */
    Result commentsByArticleId(Long articleId);

    /**
     * 发表评论
     * @param commentParam
     * @return
     */
    Result comment(CommentParam commentParam);

    /**
     * 删除文章下的评论
     * @param articleId
     * @return
     */
    Integer deleteComment(Long articleId);
}

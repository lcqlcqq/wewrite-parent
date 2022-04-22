package com.quan.wewrite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quan.wewrite.dao.mapper.ArticleMapper;
import com.quan.wewrite.dao.mapper.CommentMapper;
import com.quan.wewrite.dao.pojo.Article;
import com.quan.wewrite.dao.pojo.Comment;
import com.quan.wewrite.dao.pojo.SysUser;
import com.quan.wewrite.service.CommentsService;
import com.quan.wewrite.service.SysUserService;
import com.quan.wewrite.service.ThreadService;
import com.quan.wewrite.utils.UserThreadLocal;
import com.quan.wewrite.vo.CommentVo;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.UserVo;
import com.quan.wewrite.vo.params.CommentParam;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentsServiceImpl implements CommentsService {


    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ThreadService threadService;
    @Autowired
    private ArticleMapper articleMapper;
    @Override
    public Result commentsByArticleId(Long articleId) {
        /**
         * 1. 根据文章id 查询评论列表
         * 2. 根据作者id 查询作者信息
         * 3. 如果 level = 1 要去查询有没有子评论
         * 4. 如果有 就根据评论id查询（parent_id）
         *
         */
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId,articleId);
        queryWrapper.eq(Comment::getLevel,1);
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        return Result.success(copyList(comments));
    }

    public CommentVo copy(Comment comment){
        CommentVo commentVo = new CommentVo();
        BeanUtils.copyProperties(comment,commentVo);
        commentVo.setId(String.valueOf(comment.getId()));
        //时间格式化
        commentVo.setCreateDate(new DateTime(comment.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
        Long authorId = comment.getAuthorId();
        UserVo userVo = sysUserService.findUserVoById(authorId);
        commentVo.setAuthor(userVo);
        //评论的评论
        List<CommentVo> commentVoList = findCommentsByParentId(comment.getId());
        commentVo.setChildrens(commentVoList);
        if (comment.getLevel() > 1) {
            Long toUid = comment.getToUid();
            UserVo toUserVo = sysUserService.findUserVoById(toUid);
            commentVo.setToUser(toUserVo);
        }
        return commentVo;
    }

    private List<CommentVo> findCommentsByParentId(Long id) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId,id);
        queryWrapper.eq(Comment::getLevel,2);
        List<Comment> comments = this.commentMapper.selectList(queryWrapper);
        return copyList(comments);
    }

    public List<CommentVo> copyList(List<Comment> commentList){
        List<CommentVo> commentVoList = new ArrayList<>();
        for (Comment comment : commentList) {
            commentVoList.add(copy(comment));
        }
        return commentVoList;
    }

    @Override
    public Integer deleteComment(Long articleId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId,articleId);
        queryWrapper.eq(Comment::getLevel,1);
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        List<Long> commentIds = new ArrayList<>();
        List<Long> second_commentIds = new ArrayList<>();

        for(Comment comment : comments){
            commentIds.add(comment.getId()); //文章下的所有一级评论
            LambdaQueryWrapper<Comment> q = new LambdaQueryWrapper<>();//查询评论的评论
            q.eq(Comment::getParentId,comment.getId());
            q.eq(Comment::getLevel,2);
            List<Comment> secondCommentList = commentMapper.selectList(q);
            List<Long> secondCommentIds = new ArrayList<>();
            for(Comment c : secondCommentList){
                second_commentIds.add(c.getId());
            }
            second_commentIds.addAll(secondCommentIds);
        }

        return commentMapper.deleteBatchIds(commentIds);
    }

    @Override
    public Result comment(CommentParam commentParam) {
        SysUser sysUser = UserThreadLocal.get();
        Comment comment = new Comment();
        comment.setArticleId(commentParam.getArticleId());
        comment.setAuthorId(sysUser.getId());
        comment.setContent(commentParam.getContent());
        comment.setCreateDate(System.currentTimeMillis());
        Long parent = commentParam.getParent();
        if (parent == null || parent == 0) {
            comment.setLevel(1);
        }else{
            comment.setLevel(2);
        }
        comment.setParentId(parent == null ? 0 : parent);
        Long toUserId = commentParam.getToUserId();
        comment.setToUid(toUserId == null ? 0 : toUserId);
        this.commentMapper.insert(comment);
        //更新评论数
        Article article = articleMapper.selectById(commentParam.getArticleId());
        threadService.updateCommentCount(articleMapper,article);
        return Result.success(null);
    }
}

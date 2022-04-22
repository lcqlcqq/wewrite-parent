package com.quan.wewrite.controller;

import com.quan.wewrite.common.aop.LogAnnotation;
import com.quan.wewrite.service.CommentsService;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.CommentParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("comments")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    @GetMapping("article/{id}")
    @LogAnnotation(module = "评论",operation = "获取文章下的评论")
    public Result comments(@PathVariable("id") Long articleId){
        return commentsService.commentsByArticleId(articleId);
    }

    @PostMapping("create/change")
    @LogAnnotation(module = "评论",operation = "写评论")
    public Result comment(@RequestBody CommentParam commentParam){
        return commentsService.comment(commentParam);
    }
}

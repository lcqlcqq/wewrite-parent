package com.quan.wewrite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quan.wewrite.dao.mapper.ArticleMapper;
import com.quan.wewrite.dao.pojo.Article;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ThreadService {

    //使用线程池，异步操作，更新阅读量
    @Async("taskExecutor")
    public void updateViewCount(ArticleMapper articleMapper, Article article){
        Article articleUpdate = new Article();
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        articleUpdate.setViewCounts(article.getViewCounts() + 1);
        queryWrapper.eq(Article::getId,article.getId());
        //设置二次检测 为了在多线程之下的 线程安全
        //乐观锁：确认没被其它线程抢先修改
        queryWrapper.eq(Article::getViewCounts,article.getViewCounts());
        //相当于 update article set view_count = 100 where view_count = 99 and id = 1
        int update = articleMapper.update(articleUpdate, queryWrapper);
        while (update == 0){
            int random=(int)(Math.random()*5+1);
            try {
                Thread.sleep(random* 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Article nowArticle = articleMapper.selectById(article.getId());
            articleUpdate.setViewCounts(nowArticle.getViewCounts()+1);
            LambdaQueryWrapper<Article> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Article::getId,nowArticle.getId());
            queryWrapper1.eq(Article::getViewCounts,nowArticle.getViewCounts());
            update = articleMapper.update(articleUpdate,queryWrapper1);
        }
    }
    public void updateViewCountByRedis(Article article){

    }
    //更新评论数
    @Async("taskExecutor")
    public void updateCommentCount(ArticleMapper articleMapper,Article article){

        Article articleUpdate = new Article();
        articleUpdate.setCommentCounts(article.getCommentCounts() + 1);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getId,article.getId());
        //设置二次检测 为了在多线程之下的 线程安全
        //乐观锁：确认没被其它线程抢先修改
        queryWrapper.eq(Article::getCommentCounts,article.getCommentCounts());
        int updateResult = articleMapper.update(articleUpdate, queryWrapper);
        if(updateResult == 0){
            updateCommentCount(articleMapper,article);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

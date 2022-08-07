package com.quan.wewrite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quan.wewrite.dao.dos.Archives;
import com.quan.wewrite.dao.mapper.ArticleBodyMapper;
import com.quan.wewrite.dao.mapper.ArticleMapper;
import com.quan.wewrite.dao.mapper.ArticleTagMapper;
import com.quan.wewrite.dao.mapper.FavoritesMapper;
import com.quan.wewrite.dao.pojo.*;
import com.quan.wewrite.service.*;
import com.quan.wewrite.utils.UserThreadLocal;
import com.quan.wewrite.vo.*;
import com.quan.wewrite.vo.params.ArticleParam;
import com.quan.wewrite.vo.params.FavoritesParam;
import com.quan.wewrite.vo.params.PageParams;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private TagService tagService;
    @Autowired
    private ThreadService threadService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleBodyMapper articleBodyMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Autowired
    private FavoritesMapper favoritesMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    //
    //@Override
    //public Result listArticles(PageParams pageParams) {
    //    /**
    //     * 分页查询 article表
    //     */
    //    Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());
    //    LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
    //    if(pageParams.getCategoryId() != null){
    //        queryWrapper.eq(Article::getCategoryId,pageParams.getCategoryId());
    //    }
    //    List<Long> articleIdList = new ArrayList<>();
    //    if (pageParams.getTagId() != null){
    //        //加入标签 条件查询
    //        //article表中 没有tag字段，一篇文章可能有多个标签
    //        //article_tag  article_id 1:n tag_id
    //        LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
    //        articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
    //        List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
    //        for (ArticleTag articleTag : articleTags) {
    //            articleIdList.add(articleTag.getArticleId());
    //        }
    //        if (articleIdList.size() > 0){
    //            queryWrapper.in(Article::getId,articleIdList);  //找出有这个标签的文章，文章可能有多个标签，所以用in在标签列表里查找
    //        }
    //    }
    //    //是否置顶进行排序
    //    //order by create_date desc
    //    queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);
    //    Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
    //    List<Article> records = articlePage.getRecords();
    //    List<ArticleVo> articleVoList = copyList(records,true,true,false,false);
    //    return Result.success(articleVoList);
    //}

    /**
     * 自定义sql重写这个方法
     *
     * @param pageParams
     * @return
     */
    @Override
    public Result listArticles(PageParams pageParams) {
        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        IPage<Article> articleIPage = this.articleMapper.listArticle(
                page,
                pageParams.getCategoryId(),
                pageParams.getTagId(),
                pageParams.getYear(),
                pageParams.getMonth());
        return Result.success(copyList(articleIPage.getRecords(), true, true));
    }

    @Override
    public Result hotArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit " + limit);
        //select * id,title from article order by view_counts desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles, false, false, false, false));
    }

    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit " + limit);
        //select id,title from article order by create_date desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);

        return Result.success(copyList(articles, false, false, false, false));
    }


    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Override
    public ArticleVo findArticleById(Long id) {
        /**
         * 1. 根据id查询文章
         * 2. 根据bodyid和categoryid 关联查询
         */
        Article article = articleMapper.selectById(id);
        ArticleVo articleVo = copy(article, true, true, true, true);
        //更新阅读量，更新数据库是加写锁的，会阻塞读操作，性能就会比较低
        //更新增加了这个接口的耗时。一旦更新出问题，不能影响查看文章的操作
        //线程池，另起线程处理更新
        threadService.updateViewCount(articleMapper, article);
        return articleVo;
    }

    /**
     * 1、发布文章 目的 构建Article对象
     * 2、作者id是当前登录用户的
     * 3、如果文章id已经存在了，就是更新操作，保留几个旧数据，然后先删除3个表的内容
     * 4、如果不是更新操作，就直接插入了。
     * 5、插入标签列表。
     * 6、插入文章内容。
     */
    @Override
    @Transactional
    public Result publish(ArticleParam articleParam) {
        //此接口加入登录拦截器
        SysUser sysUser = UserThreadLocal.get();

        Long articleId = articleParam.getId();
        //查到旧的文章记录
        Article a = articleMapper.selectById(articleId);
        //新的文章
        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));
        article.setCreateDate(System.currentTimeMillis());
        article.setCommentCounts(0);
        article.setSummary(articleParam.getSummary());
        article.setTitle(articleParam.getTitle());
        article.setViewCounts(0);
        article.setWeight(Article.Article_Common);
        article.setBodyId(-1L);
        //如果旧文章存在，那么是更新操作
        if (a != null) {
            //要保留几个旧的文章数据
            Integer viewCounts = a.getViewCounts();
            Integer commentCounts = a.getCommentCounts();
            Integer weight = a.getWeight();
            //id、阅读量、评论数、置顶
            article.setId(articleId);
            article.setViewCounts(viewCounts);
            article.setCommentCounts(commentCounts);
            article.setWeight(weight);

            //删除旧的文章内容
            LambdaQueryWrapper<ArticleBody> queryBody = new LambdaQueryWrapper<>();
            queryBody.eq(ArticleBody::getArticleId, articleId);
            Long BodyId = articleBodyMapper.selectOne(queryBody).getId();
            articleBodyMapper.deleteById(BodyId);
            //删除旧的文章所有标签
            LambdaQueryWrapper<ArticleTag> queryTag = new LambdaQueryWrapper<>();
            queryTag.eq(ArticleTag::getArticleId, articleId);
            queryTag.select(ArticleTag::getId);
            List<ArticleTag> TagItems = articleTagMapper.selectList(queryTag);
            List<Long> TagIds = new ArrayList<>();
            for (ArticleTag at : TagItems) {
                TagIds.add(at.getId());
            }
            articleTagMapper.deleteBatchIds(TagIds);
            //删除旧的文章记录
            articleMapper.deleteById(articleId);
        }

        //插入之后回生成一个文章id
        this.articleMapper.insert(article);

        //tags
        List<TagVo> tags = articleParam.getTags();
        if (tags != null) {
            for (TagVo tag : tags) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(Long.parseLong(tag.getId()));
                this.articleTagMapper.insert(articleTag);
            }
        }
        //body
        ArticleBody articleBody = new ArticleBody();
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBody.setArticleId(article.getId());
        articleBodyMapper.insert(articleBody);

        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        //ArticleVo articleVo = new ArticleVo();
        //articleVo.setId(article.getId());
        //return Result.success(articleVo);
        Map<String, String> map = new HashMap<>();
        map.put("id", article.getId().toString());

        //删除文章缓存
        Set<String> keys = redisTemplate.keys("article_" + "*");
        keys.addAll(Objects.requireNonNull(redisTemplate.keys("tags_" + "*")));
        keys.addAll(Objects.requireNonNull(redisTemplate.keys("Categories_" + "*")));
        //删除缓存失败了
        if (redisTemplate.delete(keys) <= 0) {
            rocketMQTemplate.convertAndSend("blog-deleteArticle-failed", keys);
        }
        return Result.success(map);
    }

    @Override
    @Transactional
    public Result delete(Long articleId) {
        //删除文章内容
        LambdaQueryWrapper<ArticleBody> queryBody = new LambdaQueryWrapper<>();
        queryBody.eq(ArticleBody::getArticleId, articleId);
        Long BodyId = articleBodyMapper.selectOne(queryBody).getId();
        int abmd = articleBodyMapper.deleteById(BodyId);
        //删除文章所有标签记录
        LambdaQueryWrapper<ArticleTag> queryTag = new LambdaQueryWrapper<>();
        queryTag.eq(ArticleTag::getArticleId, articleId);
        queryTag.select(ArticleTag::getId);
        List<ArticleTag> TagItems = articleTagMapper.selectList(queryTag);
        List<Long> TagIds = new ArrayList<>();
        for (ArticleTag at : TagItems) {
            TagIds.add(at.getId());
        }
        int atmd = articleTagMapper.deleteBatchIds(TagIds);
        //删除文章记录
        int amd = articleMapper.deleteById(articleId);
        //删除文章所有评论
        Integer com = commentsService.deleteComment(articleId);
        //删除文章缓存
        Set<String> keys = redisTemplate.keys("article_" + "*");
        keys.addAll(Objects.requireNonNull(redisTemplate.keys("tags_" + "*")));
        keys.addAll(Objects.requireNonNull(redisTemplate.keys("Categories_" + "*")));
        //删除缓存失败了
        if (redisTemplate.delete(keys) <= 0) {
            rocketMQTemplate.convertAndSend("blog-deleteArticle-failed", keys);
        }
        return Result.success(amd);
    }

    @Override
    public Result delFavoritesArticle(FavoritesParam favoritesParam) {
        LambdaQueryWrapper<Favorites> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorites::getArticleId, favoritesParam.getArticleId());
        queryWrapper.eq(Favorites::getUserId, favoritesParam.getUserId());
        int integer = favoritesMapper.delete(queryWrapper);
        if (integer < 1) return Result.fail(0, "取消收藏失败");
        return Result.success("取消收藏成功");
    }

    @Override
    public Result getIsFavorites(FavoritesParam favoritesParam) {
        LambdaQueryWrapper<Favorites> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorites::getArticleId, favoritesParam.getArticleId());
        queryWrapper.eq(Favorites::getUserId, favoritesParam.getUserId());
        Integer integer = favoritesMapper.selectCount(queryWrapper);
        if (integer < 1) return Result.fail(0, "无收藏");
        return Result.success(integer);
    }

    @Override
    public Result addFavoritesArticle(FavoritesParam favoritesParam) {
        int insert = favoritesMapper.insert(new Favorites(Long.parseLong(favoritesParam.getUserId()), Long.parseLong(favoritesParam.getArticleId())));
        if (insert < 1) return Result.fail(-1, "收藏失败");
        return Result.success("收藏成功");
    }

    @Override
    public Result getFavoritesArticle(String id) {
        List<Article> articleList = articleMapper.listFavorites(Long.parseLong(id));
        return Result.success(copyList(articleList, true, true));
    }


    @Override
    @Transactional
    public Result setTop(Long id) {
        Article article = articleMapper.selectById(id);
        if (article.getWeight() == 0)
            article.setWeight(1);
        else
            article.setWeight(0);
        int updateRes = articleMapper.updateById(article);
        //只删除文章列表相关的缓存
        Set<String> keys = redisTemplate.keys("article_" + "*");
        //删除缓存失败了
        if (redisTemplate.delete(keys) <= 0) {
            rocketMQTemplate.convertAndSend("blog-deleteArticle-failed", keys);
            //System.out.println("已经没有缓存了");
        }
        return Result.success(updateRes);
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record, isTag, isAuthor, false, false));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record, isTag, isAuthor, isBody, false));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record, isTag, isAuthor, isBody, isCategory));
        }
        return articleVoList;
    }

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory) {
        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(String.valueOf(article.getId()));
        BeanUtils.copyProperties(article, articleVo);

        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));

        //并不是所有的接口 都需要标签 ，作者信息
        if (isTag) {
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if (isAuthor) {
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserService.findSysUserById(authorId).getNickname());
        }
        if (isBody) {
            ArticleBodyVo articleBody = findArticleBody(article.getId());
            articleVo.setBody(articleBody);
        }
        if (isCategory) {
            CategoryVo categoryVo = findCategory(article.getCategoryId());
            articleVo.setCategory(categoryVo);
        }
        return articleVo;
    }


    private CategoryVo findCategory(Long categoryId) {
        return categoryService.findCategoryById(categoryId);
    }


    private ArticleBodyVo findArticleBody(Long articleId) {
        LambdaQueryWrapper<ArticleBody> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleBody::getArticleId, articleId);
        ArticleBody articleBody = articleBodyMapper.selectOne(queryWrapper);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }


}

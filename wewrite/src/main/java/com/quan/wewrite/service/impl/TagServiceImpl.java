package com.quan.wewrite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quan.wewrite.dao.mapper.TagMapper;
import com.quan.wewrite.dao.pojo.Tag;
import com.quan.wewrite.service.TagService;
import com.quan.wewrite.vo.ErrorCode;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.TagVo;
import com.quan.wewrite.vo.params.TagParam;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class TagServiceImpl implements TagService {
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public TagVo copy(Tag tag){
        TagVo tagVo = new TagVo();
        BeanUtils.copyProperties(tag,tagVo);
        tagVo.setId(String.valueOf(tag.getId()));
        return tagVo;
    }
    public List<TagVo> copyList(List<Tag> tagList){
        List<TagVo> tagVoList = new ArrayList<>();
        for (Tag tag : tagList) {
            tagVoList.add(copy(tag));
        }
        return tagVoList;
    }
    @Override
    public List<TagVo> findTagsByArticleId(Long articleId) {
        //mybatis-plus无法多表查询
        List<Tag> tags = tagMapper.findTagsByArticleId(articleId);
        return copyList(tags);
    }

    public Result hots(int limit){
        /**
         * 1. 标签对应的文章数最多的是最热标签
         * 2. 根据tag_id分组，计数，排序取前limit个
         */
        List<Long> tagIds = tagMapper.findHotsTagIds(limit);
        if (CollectionUtils.isEmpty(tagIds)){
            return Result.success(Collections.emptyList());
        }
        // 需要 id,tag_name
        List<Tag> tagList = tagMapper.findTagsByIds(tagIds);
        return Result.success(tagList);
    }

    /**
     * 查询文章的所有标签
     * @return
     */
    @Override
    public Result findAll() {
        List<Tag> tags = this.tagMapper.selectList(new LambdaQueryWrapper<>());
        return Result.success(copyList(tags));
    }

    /**
     * 同上
     * @return
     */
    @Override
    public Result findAllDetail() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        List<Tag> tags = this.tagMapper.selectList(queryWrapper);
        return Result.success(copyList(tags));
    }


    /**
     * 查询包含某个标签的所有文章
     * @param id
     * @return
     */
    @Override
    public Result findDetailById(Long id) {
        Tag tag = tagMapper.selectById(id);
        TagVo copy = copy(tag);
        return Result.success(copy);
    }

    @Override
    public Result addTag(TagParam tagParam) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getTagName,tagParam.getTagName());
        if(tagMapper.selectCount(queryWrapper)!=0){
            return Result.fail(ErrorCode.TAG_NAME_DUPLICATED.getCode(),ErrorCode.TAG_NAME_DUPLICATED.getMsg());
        }
        Tag tag = new Tag();
        tag.setTagName(tagParam.getTagName());
        tag.setAvatar(tagParam.getAvatar());
        //把类别缓存删掉
        if(tagMapper.insert(tag) > 0){
            Set<String> keys = redisTemplate.keys("tags_" + "*");
            redisTemplate.delete(keys);
        }
        return Result.success("标签新建成功");
    }

    @Override
    public Result modifyTag(TagParam tagParam) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getId,tagParam.getId());
        if(tagMapper.selectCount(queryWrapper) == 0){
            return Result.fail(ErrorCode.TAG_NOT_EXIST.getCode(),ErrorCode.TAG_NOT_EXIST.getMsg());
        }
        Tag tag = new Tag();
        tag.setId(Long.parseLong(tagParam.getId()));
        tag.setTagName(tagParam.getTagName());
        tag.setAvatar(tagParam.getAvatar());

        tagMapper.updateById(tag);
        //把类别缓存删掉
        Set<String> keys = redisTemplate.keys("tags_" + "*");
        redisTemplate.delete(keys);
        return Result.success("标签修改成功");
    }

    @Override
    public Result deleteTag(String id) {
        if(id.equals("1")) return Result.fail(-1,"系统错误");
        if(tagMapper.deleteById(id) > 0 && tagMapper.deleteTagRecord(Long.parseLong(id)) > 0){

            //删除类别缓存
            Set<String> keys = redisTemplate.keys("tags_" + "*");
            //删除缓存失败了
            if(redisTemplate.delete(keys) <= 0) {
                rocketMQTemplate.convertAndSend("blog-deleteArticle-failed",keys);
            }
            return Result.success("标签删除成功");
        }
        return Result.fail(-1,"标签删除失败");
    }
}

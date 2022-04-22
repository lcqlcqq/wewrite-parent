package com.quan.wewrite.controller;

import com.quan.wewrite.common.aop.LogAnnotation;
import com.quan.wewrite.common.cache.Cache;
import com.quan.wewrite.service.TagService;
import com.quan.wewrite.vo.Result;
import com.quan.wewrite.vo.params.TagParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tags")
public class TagsController {
    @Autowired
    private TagService tagService;

    @GetMapping("hot")
    @Cache(name = "tags_hot")
    @LogAnnotation(module = "标签",operation = "获取最热标签")
    public Result hot(){
        int limit = 6;
        return tagService.hots(limit);
    }

    @GetMapping
    @Cache(name = "tags_all")
    @LogAnnotation(module = "标签",operation = "查询所有标签列表")
    public Result findAll(){
        return tagService.findAll();
    }

    @GetMapping("detail")
    @Cache(name = "tags_detail_all")
    @LogAnnotation(module = "标签",operation = "查询所有标签列表")
    public Result findAllDetail(){
        return tagService.findAllDetail();
    }

    @GetMapping("detail/{id}")
    @Cache(name = "tags_articles")
    @LogAnnotation(module = "标签",operation = "查询对应标签下的所有文章")
    public Result findDetailById(@PathVariable("id") Long id){
        return tagService.findDetailById(id);
    }
    @PostMapping("add")
    public Result addTag(@RequestBody TagParam tagParam){
        return tagService.addTag(tagParam);
    }
    @PostMapping("modify")
    public Result modifyTag(@RequestBody TagParam tagParam){
        return tagService.modifyTag(tagParam);
    }
    @PostMapping("delete/{id}")
    public Result deleteTag(@PathVariable("id") String id){
        return tagService.deleteTag(id);
    }
}

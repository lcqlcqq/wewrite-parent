package com.quan.wewrite.vo.params;

import com.quan.wewrite.vo.CategoryVo;
import com.quan.wewrite.vo.TagVo;
import lombok.Data;

import java.util.List;

@Data
public class ArticleParam {

    private Long id;

    private ArticleBodyParam body;

    private CategoryVo category;

    private String summary;

    private List<TagVo> tags;

    private String title;
}

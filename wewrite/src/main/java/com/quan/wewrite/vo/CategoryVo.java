package com.quan.wewrite.vo;

import lombok.Data;
//类别
@Data
public class CategoryVo {
    //@JsonSerialize(using = ToStringSerializer.class)
    private String id;

    private String avatar;

    private String categoryName;

    private String description;
}

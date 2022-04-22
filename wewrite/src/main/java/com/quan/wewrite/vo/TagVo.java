package com.quan.wewrite.vo;

import lombok.Data;

@Data
public class TagVo {
    //@JsonSerialize(using = ToStringSerializer.class)
    private String id;

    private String tagName;

    private String avatar;
}

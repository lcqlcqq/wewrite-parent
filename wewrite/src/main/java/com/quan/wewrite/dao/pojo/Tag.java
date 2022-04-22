package com.quan.wewrite.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Tag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String avatar;

    private String tagName;

}

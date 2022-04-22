package com.quan.wewrite.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.Data;
@Data
public class Favorites {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long articleId;

    public Favorites(){}
    public Favorites(Long userId, Long articleId) {
        this.userId = userId;
        this.articleId = articleId;
    }
}

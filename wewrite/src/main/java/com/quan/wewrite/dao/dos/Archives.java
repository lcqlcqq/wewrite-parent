package com.quan.wewrite.dao.dos;

import lombok.Data;

//dos 从数据库查出来的 不需要持久化
@Data
public class Archives {

    private Integer year;

    private Integer month;

    private Integer count;
}

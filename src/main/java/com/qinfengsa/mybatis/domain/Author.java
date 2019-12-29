package com.qinfengsa.mybatis.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 作者
 * @author: qinfengsa
 * @date: 2019/5/17 18:28
 */
@Data
public class Author implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = 8121084591710089348L;
    /**
     * 作者ID
     */
    Integer authorId;

    /**
     * 作者名称
     */
    String authorName;
}

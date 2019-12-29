package com.qinfengsa.mybatis.domain;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Blog 测试类 博客文章
 * @author: qinfengsa
 * @date: 2019/5/17 16:44
 */
@Data
@ToString
public class Blog implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = -3345923163017680774L;

    /**
     * 文章ID
     */
    private Integer bid;

    /**
     * 文章标题
     */
    private String name;

    /**
     * 文章作者ID
     */
    private Integer authorId;

}

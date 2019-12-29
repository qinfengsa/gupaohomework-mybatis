package com.qinfengsa.mybatis.domain.associate;

import java.io.Serializable;
import java.util.List;

/**
 * 博客文章
 * @author: qinfengsa
 * @date: 2019/5/17 18:31
 */
public class AuthorAndBlog implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = -879958418680137577L;

    /**
     * 作者ID
     */
    Integer authorId;

    /**
     * 作者名称
     */
    String authorName;

    /**
     * 文章和评论列表
     */
    List<BlogAndComment> blog;

}

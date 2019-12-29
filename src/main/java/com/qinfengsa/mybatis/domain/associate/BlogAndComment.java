package com.qinfengsa.mybatis.domain.associate;

import com.qinfengsa.mybatis.domain.Comment;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: qinfengsa
 * @date: 2019/5/17 18:30
 */
@Data
public class BlogAndComment implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = -2899675596418538287L;

    /**
     * 文章ID
     */
    Integer bid;

    /**
     * 文章标题
     */
    String name;

    /**
     * 文章作者ID
     */
    Integer authorId;

    /**
     * 文章评论
     */
    List<Comment> comment;
}

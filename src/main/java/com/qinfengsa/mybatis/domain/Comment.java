package com.qinfengsa.mybatis.domain;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * 评论
 * @author: qinfengsa
 * @date: 2019/5/17 18:38
 */
@Data
@Alias("comment")
public class Comment implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = 2649374520758362679L;
    /**
     * 评论ID
     */
    Integer commentId;

    /**
     * 所属文章ID
     */
    Integer bid;

    /**
     * 内容
     */
    String content;
}

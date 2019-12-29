package com.qinfengsa.mybatis.domain.associate;

import com.qinfengsa.mybatis.domain.Author;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: qinfengsa
 * @date: 2019/5/17 18:30
 */
@Data
public class BlogAndAuthor implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = -38365130129501073L;
    /**
     * 文章ID
     */
    Integer bid;

    /**
     * 文章标题
     */
    String name;

    /**
     * 作者
     */
    Author author;
}

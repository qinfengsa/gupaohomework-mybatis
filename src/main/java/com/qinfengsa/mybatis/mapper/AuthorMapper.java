package com.qinfengsa.mybatis.mapper;

import com.qinfengsa.mybatis.domain.Author;

/**
 * @author: qinfengsa
 * @date: 2019/5/19 15:27
 */
public interface AuthorMapper {

    Author selectAuthor(Integer authorId);
}

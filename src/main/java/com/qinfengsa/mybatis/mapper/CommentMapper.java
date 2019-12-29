package com.qinfengsa.mybatis.mapper;

import com.qinfengsa.mybatis.domain.Comment;

import java.util.List;

/**
 * @author: qinfengsa
 * @date: 2019/5/19 15:27
 */
public interface CommentMapper {

    List<Comment> selectComment(Integer bid);
}

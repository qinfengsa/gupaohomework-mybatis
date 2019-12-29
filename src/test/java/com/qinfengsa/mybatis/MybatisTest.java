package com.qinfengsa.mybatis;

import com.qinfengsa.mybatis.domain.Blog;
import com.qinfengsa.mybatis.domain.associate.BlogAndComment;
import com.qinfengsa.mybatis.mapper.BlogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author: qinfengsa
 * @date: 2019/5/17 16:33
 */
@Slf4j
public class MybatisTest {

    /**
     * 逻辑分页
     * @throws IOException
     */
    @Test
    public void testSelectByRowBounds() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        SqlSession session = sqlSessionFactory.openSession();
        try {
            BlogMapper mapper = session.getMapper(BlogMapper.class);
            int start = 0; // offset
            int pageSize = 5; // limit
            RowBounds rb = new RowBounds(start, pageSize);
            List<Blog> list = mapper.selectBlogList(rb); // 使用逻辑分页
            for(Blog b :list){
                log.debug("result:{}",b);
            }
        } finally {
            session.close();
        }
    }


    /**
     * 打印SQL
     * @throws IOException
     */
    @Test
    public void logSql() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        SqlSession session = sqlSessionFactory.openSession();
        try {
            BlogMapper mapper = session.getMapper(BlogMapper.class);
            Blog b = mapper.selectBlog(1);
            log.debug("result:{}",b);
        } finally {
            session.close();
        }
    }



}

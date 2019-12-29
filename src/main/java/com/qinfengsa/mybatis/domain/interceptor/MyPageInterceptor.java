package com.qinfengsa.mybatis.domain.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 分页拦截
 * @author: qinfengsa
 * @date: 2019/12/27 09:52
 */
@Intercepts({
    // 拦截Executor的query方法,方法的参数{MappedStatement,参数,分页,结果处理}
    @Signature(type = Executor.class,method = "query",
            args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "query",
            args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
@Slf4j
public class MyPageInterceptor implements Interceptor {

    /**
     * 拦截器执行方法
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        // 参数
        Object parameterObject = args[1];
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        BoundSql boundSql;
        if (args.length == 6) {
            boundSql = (BoundSql)args[5];
        } else {
            boundSql = ms.getBoundSql(parameterObject);
        }

        RowBounds rowBounds = (RowBounds) args[2];
        if (rowBounds == RowBounds.DEFAULT) {
            return invocation.proceed();
        }

        StringBuilder sql = new StringBuilder(boundSql.getSql());
        sql.append(String.format(" LIMIT %d,%d", rowBounds.getOffset(), rowBounds.getLimit())) ;

        // 自定义sqlSource
        SqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), sql.toString(), boundSql.getParameterMappings());

        // 反射
        metaObject.setValue("sqlSource", sqlSource);
        return invocation.proceed();
    }
}

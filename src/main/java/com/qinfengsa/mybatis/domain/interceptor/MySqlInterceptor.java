package com.qinfengsa.mybatis.domain.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * SQL 语句拦截
 * @author: qinfengsa
 * @date: 2019/12/27 09:52
 */
@Intercepts({
        @Signature(type = Executor.class,method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        // 拦截Executor的query方法,方法的参数{MappedStatement,参数,分页,结果处理器}
        @Signature(type = Executor.class,method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        // 拦截Executor的update方法,方法的参数{MappedStatement,参数}
        @Signature(type = Executor.class,method = "update",
        args = {MappedStatement.class, Object.class})
})
@Slf4j
public class MySqlInterceptor implements Interceptor {

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
        BoundSql boundSql;
        if (args.length == 6) {
            boundSql = (BoundSql)args[5];
        } else {
            boundSql = ms.getBoundSql(parameterObject);
        }
        // 打印SQL语句
        logSql(boundSql,parameterObject,ms.getConfiguration());
        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        }finally {
            long endTime = System.currentTimeMillis();
            log.debug("SQL执行耗时：{} ms",(endTime-startTime));
        }
    }



    /**
     * 打印 SQL
     * @param boundSql
     * @param parameterObject
     * @param configuration
     */
    private void logSql(BoundSql boundSql, Object parameterObject, Configuration configuration) {
        StringBuilder sql = new StringBuilder(boundSql.getSql());
        List<String> parameters = new ArrayList<>();
        /**
         * 参考MyBatis处理参数的语句
         * @see org.apache.ibatis.scripting.defaults.DefaultParameterHandler#setParameters
         */
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (ParameterMapping parameterMapping : parameterMappings) {
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                Object value;
                String propertyName = parameterMapping.getProperty();
                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                if (value instanceof Number) {
                    parameters.add(String.valueOf(value));
                } else if (value instanceof Date) {
                    parameters.add("'" + formatter.format(value) + "'");
                } else if (value instanceof String) {
                    parameters.add("'" + value.toString() + "'");
                }
            }
        }
        // 替换参数
        for (String value :parameters) {
            int index = sql.indexOf("?");
            sql.replace(index, index + 1, value);
        }
        log.debug("获取SQL语句：{}",sql.toString());
    }
}

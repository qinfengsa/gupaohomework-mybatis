### 装饰模式  

CachingExecutor内部持有别的Executor类delegate，对update和query方法最终都交给delegate执行

~~~java
public class CachingExecutor implements Executor {

  	private final Executor delegate;
    // 在构造时传入Executor(SimpleExecutor,ReuseExecutor,BatchExecutor)
    // update和query最终都交给delegate
    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
        delegate.setExecutorWrapper(this);
    }
    @Override
    public int update(MappedStatement ms, Object parameterObject) throws SQLException {
        flushCacheIfRequired(ms);
        // 最终由delegate执行update方法
        return delegate.update(ms, parameterObject);
    }
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, 
                             ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
        throws SQLException {
        Cache cache = ms.getCache();
        if (cache != null) {
            flushCacheIfRequired(ms);
            if (ms.isUseCache() && resultHandler == null) {
                ensureNoOutParams(ms, boundSql);
                @SuppressWarnings("unchecked")
                List<E> list = (List<E>) tcm.getObject(cache, key);
                if (list == null) {
                    list = delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                    tcm.putObject(cache, key, list); // issue #578 and #116
                }
                return list;
            }
        }
        // 最终由delegate执行query方法
        return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }
}
~~~

### 建造者模式

建造者模式可以让我们使用多个简单的对象一步一步构建成一个复杂的对象

~~~java
public class SqlSessionFactoryBuilder {    
	public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        try {
            // XMLConfigBuilder 读取XML配置信息
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public SqlSessionFactory build(Configuration config) {
        // 通过配置类config构造SqlSessionFactory
        return new DefaultSqlSessionFactory(config);
    }
}
~~~



### 工厂模式　

SqlSessionFactory负责生产对应的SqlSession，通过不同的工厂生产不同的SqlSession

~~~java
public interface SqlSessionFactory { 
  	SqlSession openSession();
}
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    @Override
    public SqlSession openSession() {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
    }
    // SqlSession的构造过程很复杂,工厂模式使我们不需要关注构造过程中的细节
    private SqlSession openSessionFromDataSource(ExecutorType execType, 
         TransactionIsolationLevel level, boolean autoCommit) {
        Transaction tx = null;
        try {
            final Environment environment = configuration.getEnvironment();
            final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            final Executor executor = configuration.newExecutor(tx, execType);
            return new DefaultSqlSession(configuration, executor, autoCommit);
        } catch (Exception e) {
            closeTransaction(tx); // may have fetched a connection so lets call close()
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }
}
~~~



### 代理模式　

MyBatis的插件功能通过JDK动态代理实现，核心Plugin.wrap方法，使用JDK动态代理创建代理对象， Plugin对象实现了InvocationHandler接口，会调用invoke方法

~~~java
public class Plugin implements InvocationHandler {
    // 目标对象
    private final Object target;
    // interceptor对象
    private final Interceptor interceptor;
    // 记录了 @Signature注解中的信息
    private final Map<Class<?>, Set<Method>> signatureMap;

    private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    public static Object wrap(Object target, Interceptor interceptor) {
        // 获取 Interceptor 中 Signature注解的信息
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        // 获取目标类型
        Class<?> type = target.getClass();
        // 获取目标类型实现的接口
        Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
        if (interfaces.length > 0) {
            // 使用JDK动态代理创建代理对象, Plugin对象实现了InvocationHandler接口,调用invoke方法
            return Proxy.newProxyInstance(
                type.getClassLoader(),
                interfaces,
                new Plugin(target, interceptor, signatureMap));
        }
        return target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            // 获取 可被当前 Interceptor 拦截的方法
            Set<Method> methods = signatureMap.get(method.getDeclaringClass());
            // 需要被拦截,则调用 interceptor.intercept() 方法进行拦截处理
            if (methods != null && methods.contains(method)) {
                return interceptor.intercept(new Invocation(target, method, args));
            }
            // 不需要被拦截,则调用 target 对象的相应方法
            return method.invoke(target, args);
        } catch (Exception e) {
            throw ExceptionUtil.unwrapThrowable(e);
        }
    }
}
~~~





### 模板模式　

BaseExecutor提供了模板，由SimpleExecutor,ReuseExecutor,BatchExecutor具体实现功能

~~~java
public abstract class BaseExecutor implements Executor {
    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        // 判断当前的Executor是否已经关闭
        ErrorContext.instance().resource(ms.getResource())
            .activity("executing an update").object(ms.getId());
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        // 清除一级缓存
        clearLocalCache();
        // doUpdate抽象方法,交给子类重写
        return doUpdate(ms, parameter);
    }
    // 抽象方法,交给子类重写
    protected abstract int doUpdate(MappedStatement ms, Object parameter)
      throws SQLException;
}
~~~



### 责任链模式

MyBatis

~~~java
public class InterceptorChain {

    private final List<Interceptor> interceptors = new ArrayList<>();

    public Object pluginAll(Object target) {
        // 遍历插件集合
        for (Interceptor interceptor : interceptors) {
            // 调用 Interceptor.plugin()方法
            target = interceptor.plugin(target);
        }
        return target;
    }
}
~~~



### 策略模式

Executor根据不同的配置选择相应的执行器

~~~java
public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    // 根据参数 <setting name="defaultExecutorType" value="SIMPLE"/>
    // 选择 Executor 实现
    if (ExecutorType.BATCH == executorType) {
        executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
        executor = new ReuseExecutor(this, transaction);
    } else {
        executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) {
        // 开启二级缓存
        executor = new CachingExecutor(executor);
    }

    // 通过 InterceptorChain.pluginAll()方法创建 Executor的代理对象
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
}
~~~

### 单例模式

Configuration记录了MyBatis的配置信息，全局只有一个实例


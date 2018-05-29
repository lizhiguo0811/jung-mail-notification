package ren.jung.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Spring的工具类
 * <p>
 * create by jung at 2017年12月15日10:59:27
 */
@Component
public class SpringUtils implements ApplicationContextAware, EnvironmentAware {

    private static ApplicationContext applicationContext;
    private static Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtils.applicationContext == null) {
            SpringUtils.applicationContext = applicationContext;
        }
    }




    /**
     * 取applicationContext
     *
     * @return 获取到的context
     * @author Jung
     * @since 2017年12月15日10:55:43
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name 需要获取的bean的名称
     * @return 获取到的bean
     * @author Jung
     * @since 2017年12月15日10:56:20
     */
    public static Object getBean(String name) {
        checkBean();
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean
     *
     * @param clazz 需要获取的类型
     * @param <T>   class的类型
     * @return 获取到的bean
     * @author Jung
     * @since 2017年12月15日10:57:44
     */
    public static <T> T getBean(Class<T> clazz) {
        checkBean();
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name  需要获取的bean的名称
     * @param clazz 需要获取的类型
     * @param <T>   class的类型
     * @return 获取到的bean
     * @author Jung
     * @since 2017年12月15日10:56:20
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        checkBean();
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 检测操作的合法性，如果不合法，那么会抛出异常
     *
     * @author Jung
     * @since 2017年12月15日11:00:17
     */
    private static void checkBean() {
        if (applicationContext == null) {
            throw new BeanInitializationException("spring没有初始化，请检查！");
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        if (SpringUtils.environment != null) {
            SpringUtils.environment = environment;
        }
    }

    /**
     * 获取相应的环境变量
     *
     * @param key 需要获取的key
     * @return 获取到的变量名
     * @author Jung
     * @since 2017年12月24日14:50:33
     */
    public static String getEnvironment(String key) {
        if (environment != null) {
            return environment.getProperty(key);
        }
        return null;
    }
}
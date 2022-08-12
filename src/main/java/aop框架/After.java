package aop框架;

import java.lang.annotation.*;

/**
 * value填的是需要代理的类
 * 需要代理的类必须写上@Component注解且与Appication在同一个包下
 * 一个value目前只支持一个@After方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface After {
    Class<?> value();
}
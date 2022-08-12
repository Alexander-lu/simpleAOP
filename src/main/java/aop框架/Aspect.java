package aop框架;

import java.lang.annotation.*;

/**
 * 切面注解
 * 要求：要想@Before生效，必须在@Before所在的类上写@Aspect注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Aspect {
}

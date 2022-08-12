package userTest;

import aop框架.After;
import aop框架.Aspect;
import aop框架.Before;

@Aspect
public class Aspect1 {
    @Before(value = BookDaoImpl.class)
    public void before(){
        System.out.println("欢迎使用Aspect");
    }
    @After(value = BookDaoImpl.class)
    public void after(){
        System.out.println("Aspect使用完毕");
    }
}

package userTest;

import aop框架.DIContainer;
import aop框架.SpringApplication;

public class Application {
    public static void main(String[] args) {
        DIContainer diContainer = SpringApplication.run(Application.class);
        BookDaoImpl 简单bean = (BookDaoImpl)diContainer.简单bean.get(BookDaoImpl.class);
        BookDaoImpl 代理bean = (BookDaoImpl)diContainer.代理bean.get(BookDaoImpl.class);
        简单bean.save();
        代理bean.save();
    }
}

package userTest;

import aop框架.Component;

@Component
public class BookDaoImpl {

    public void save(){
        System.out.println("保存图书...");
    }
    public void update(){
        System.out.println("更新图书...");
    }
}
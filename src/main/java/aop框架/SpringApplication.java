package aop框架;

import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpringApplication {
    public static DIContainer run(Class<?> clazz) {
        //新建一个容器
        DIContainer diContainer = new DIContainer();
        //找到clazz所在的包下的所有类，进行SpringBoot的自动装配
        List<Class<?>> allClassesInSamePackage = findAllClassesInSamePackage(clazz);
        //先找到所有的Aspect类进行处理，剩下的类暂时不处理，保存起来
        List<Class<?>> leftClasses = new ArrayList<>();
        for (Class<?> aClass : allClassesInSamePackage)
        {
            if (aClass.getAnnotation(Aspect.class)!=null)
            {
                //构造aspect对象
                Constructor constructor = noArgConstructor(aClass);
                Object aspect= newInstance(constructor);
                //将构造的aspect对象保存起来（单例模式）
                diContainer.所有aspect对象的集合.add(aspect);
                //遍历aspect类的所有方法，找到before方法
                Method[] declaredMethods = aClass.getDeclaredMethods();
                for (Method declaredMethod : declaredMethods)
                {
                    if(declaredMethod.getAnnotation(Before.class)!=null)
                    {
                        //找到@Before标记的需要代理的类
                        Class<?> value = declaredMethod.getAnnotation(Before.class).value();
                        //将@Before方法和invoke需要的参数Object aspect保存起来
                        diContainer.根据需要代理的类找aspect对象.put(value,aspect);
                        diContainer.根据需要代理的类找before方法.put(value,declaredMethod);
                    }else if(declaredMethod.getAnnotation(After.class)!=null)
                    {
                        //找到@After标记的需要代理的类
                        Class<?> value = declaredMethod.getAnnotation(After.class).value();
                        //将@After方法和invoke需要的参数Object aspect保存起来
                        diContainer.根据需要代理的类找aspect对象.put(value,aspect);
                        diContainer.根据需要代理的类找After方法.put(value,declaredMethod);
                    }
                }
            }else {
                //剩下的类暂时不处理，保存起来
                leftClasses.add(aClass);
            }
        }
        //对剩下的类进行处理
        for (Class<?> leftClass : leftClasses) {
            //找到需要代理/依赖注入的类
            if (leftClass.getAnnotation(Component.class)!=null)
            {
                //用反射调用component的无参构造方法构造简单的component对象
                Constructor constructor = noArgConstructor(leftClass);
                Object component= newInstance(constructor);
                //将简单的component对象放入容器
                diContainer.简单bean.put(leftClass,component);
                System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "D:\\java\\aopaop\\src\\main\\java\\aop框架");
                //创建CGLIB核心的类
                Enhancer enhancer=new Enhancer();
                //设置父类
                enhancer.setSuperclass(leftClass);
                //从dic容器里面找到@Before所标记的方法和invoke需要的Object对象
                Object aspect = diContainer.根据需要代理的类找aspect对象.get(leftClass);
                Method beforeMethod = diContainer.根据需要代理的类找before方法.get(leftClass);
                Method afterMethod = diContainer.根据需要代理的类找After方法.get(leftClass);
                //设置回调函数
                enhancer.setCallback(new MethodInterceptor() {
                    @Override
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                        if (beforeMethod!=null) {
                            beforeMethod.invoke(aspect);
                        }
                        Object ret = methodProxy.invokeSuper(obj, args);
                        if (afterMethod != null) {
                            afterMethod.invoke(aspect);
                        }
                        return ret;
                    }
                });
                //生成代理对象
                Object proxy = enhancer.create();
                //将代理对象放入容器
                diContainer.代理bean.put(leftClass,proxy);
            }
        }
        return diContainer;
    }
    //获取clazz所在的包下的所有类的路径
    public static List<Class<?>> findAllClassesInSamePackage(Class<?> clazz){
        Package aPackage = clazz.getPackage();
        String aPackageName = aPackage.getName();
        List< Class<?> > returnList = new ArrayList<>();
        List<Path> packagePaths = getPackagePaths(clazz);
        for (Path packagePath : packagePaths) {
            File file = new File(String.valueOf(packagePath));
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File innerFile : files) {
                    String name = innerFile.getName();
                    if (name.endsWith("class")) {
                        String substring = name.substring(0, name.length() - 6);
                        Class<?> aClass = getClass(aPackageName+"."+substring);
                        returnList.add(aClass);
                    }
                }
            }
        }
        return returnList;
    }
    //获取包路径
    public static List<Path> getPackagePaths(Class<?> clazz) {
        var packageName = clazz.getPackageName();
        var folderName = packageName.replace('.', File.separatorChar);
        var classLoader = clazz.getClassLoader();
        try {
            return Collections.list(classLoader.getResources(folderName)).stream().map(url -> {
                try {
                    return Path.of(url.toURI());
                } catch (URISyntaxException e) {
                    throw new RuntimeException("无法查找" + packageName + "中的类");
                }
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("无法查找" + packageName + "中的类");
        }
    }
    //根据类的全限定名称获取类对象
    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getCause());
        }
    }
    //获取某个类的无参构造函数（必须是public的）
    public static <T> Constructor<T> noArgConstructor(Class<T> clazz) {
        try {
            return clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("找不到类" + clazz.getName() + "的公开无参构造函数");
        }
    }
    //调用类的构造函数，返回该类的一个实例
    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}


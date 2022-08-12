package aop框架;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DIContainer {
    public Map<Class<?>, Object> 简单bean = new HashMap<>();
    public Map<Class<?>, Object> 代理bean = new HashMap<>();
    public Map<Class<?>, Method> 根据需要代理的类找before方法 = new HashMap<>();
    public Map<Class<?>, Method> 根据需要代理的类找After方法 = new HashMap<>();
    public List<Object> 所有aspect对象的集合 = new ArrayList<>();
    public Map<Class<?>, Object> 根据需要代理的类找aspect对象 = new HashMap<>();
}
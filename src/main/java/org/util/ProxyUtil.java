package org.util;

import org.anno.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProxyUtil {

    private static final Class<?> objArrClass = Object[].class;

    public static Map<String, Method> getAopMethod(Object obj, AopBase aopBase) {
        var annoClass = switch (aopBase) {
            case BEFORE -> Before.class;
            case AFTER -> After.class;
            case AFTERRETURN -> AfterReturn.class;
            case AROUND -> Around.class;
        };

        var map = new HashMap<String, Method>();
        Arrays.stream(obj.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annoClass))
                .forEach(method -> {
                    String name = null;
                    Annotation anno = method.getAnnotation(annoClass);
                    if (Before.class.equals(annoClass)) {
                        name = ((Before) anno).value();
                    } else if (After.class.equals(annoClass)) {
                        name = ((After) anno).value();
                    } else if (AfterReturn.class.equals(annoClass)) {
                        name = ((AfterReturn) anno).value();
                    } else {
                        name = ((Around) anno).value();
                    }
                    map.put(name, method);
                });
        return map;
    }

    public static Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("无参构造无法调用");
        }
    }

    public static void invokeNormalMethod(Method method, Object invoker, Object[] args) {
        if (method.getParameterCount() == 0) {
            try {
                method.invoke(invoker, null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("该方法无法被无参调用");
            }
        } else if (method.getParameterCount() == 1 && method.getParameters()[0].getType() == Object[].class) {
            try {
                method.invoke(invoker, (Object) args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("该方法无法被调用，类型为Object[]的参数将被填充为原函数的参数");
            }
        } else {
            throw new RuntimeException("无法调用，请确保是无参或者单独参数类型为Object[]");
        }
    }

    public static Object invokeAfterReturn(Method method, Object invoker, Object arg) {

        if (method.getParameterCount() != 1) {
            throw new RuntimeException("该函数不符合AfterReturn的要求");
        }

        if (method.getParameters()[0].getType() != Object.class
                && method.getReturnType() != Object.class) {
            throw new RuntimeException("该函数不符合AfterReturn的要求");
        }

        try {
            return method.invoke(invoker, (Object) arg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("无法调用该函数");
        }
    }

    public static Object invokeAround(Method method, Object invoker, JoinPoint joinPoint) {
        try {
            return method.invoke(invoker, joinPoint);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("此函数不符合Around函数的要求");
        }
    }

}

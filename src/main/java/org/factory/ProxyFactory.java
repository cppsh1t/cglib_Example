package org.factory;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.anno.AopBase;
import org.anno.JoinPoint;
import org.util.ProxyUtil;
import java.lang.reflect.Method;
import java.util.stream.Collectors;


public class ProxyFactory {
    private final Enhancer enhancer = new Enhancer();


    public <T> T makeProxy(T origin, Object aopObj) {
        var beforeMethods = ProxyUtil.getAopMethod(aopObj, AopBase.BEFORE);
        var afterMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AFTER);
        var afterReturnMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AFTERRETURN);
        var aroundMethods = ProxyUtil.getAopMethod(aopObj, AopBase.AROUND);
        var clazz = origin.getClass();

        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object no, Method method, Object[] args, MethodProxy no2) throws Throwable {
                Object result = null;

                beforeMethods.keySet().stream()
                        .filter(name -> name.equals(method.getName()))
                        .map(beforeMethods::get)
                        .forEach(m -> ProxyUtil.invokeNormalMethod(m, aopObj, args));

                var aroundMethod = aroundMethods.keySet().stream()
                        .filter(name -> name.equals(method.getName()))
                        .map(aroundMethods::get).findFirst().orElse(null);
                if (aroundMethod != null) {
                    var point = new JoinPoint(origin, method, args);
                    result = ProxyUtil.invokeAround(aroundMethod, aopObj, point);
                } else {
                    result = method.invoke(origin, args);
                }

                if (result != null) {
                    var afm = afterReturnMethods.keySet().stream()
                            .filter(name -> name.equals(method.getName()))
                            .map(afterReturnMethods::get).collect(Collectors.toList());
                    for(var m : afm) {
                        result = ProxyUtil.invokeAfterReturn(m, aopObj, result);
                    }
                }

                afterMethods.keySet().stream()
                        .filter(name -> name.equals(method.getName()))
                        .map(afterMethods::get)
                        .forEach(m -> ProxyUtil.invokeNormalMethod(m, aopObj, args));
                return result;
            }
        });

        return (T) enhancer.create();
    }

}

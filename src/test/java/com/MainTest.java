package com;

import com.entity.Foo;
import com.entity.FooAOP;
import org.factory.ProxyFactory;


public class MainTest {

    public static void main(String[] args) {
        Foo origin = new Foo();
        FooAOP fooAOP = new FooAOP();
        var proxyFactory = new ProxyFactory();
        var foo = proxyFactory.makeProxy(origin, fooAOP);
        System.out.println(foo.introduce("shit"));

    }

}

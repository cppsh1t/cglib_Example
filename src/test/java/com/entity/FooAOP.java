package com.entity;

import org.anno.*;

import java.util.Arrays;

@Aspect(Foo.class)
public class FooAOP {

    @Before("introduce")
    public void beforeIntroduce(Object[] args) {
        System.out.println("this is before introduce");
    }

    @After("introduce")
    public void afterIntroduce() {
        System.out.println("this is after introduce");
    }

    @AfterReturn("introduce")
    public Object afterReturnIntroduce(Object obj) {
        return "holy shit";
    }

//    @Around("introduce")
//    public Object aroundIntroduce(JoinPoint joinPoint) {
//        return "OTTO!";
//    }
}

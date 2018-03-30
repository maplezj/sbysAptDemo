package com.example.zhaojian.sbysaptdemo;

import com.example.annotation.METHOD;

import java.util.List;
import java.util.Set;

/**
 * Created by zhaojian on 2018/3/29.
 */

public interface RequestAPI
{
    @METHOD("value1")
    List<Set<Integer>> test(int inputTest);

/*    @METHOD("value2")
    List<Set<String>> test22(String stringtest);*/
}

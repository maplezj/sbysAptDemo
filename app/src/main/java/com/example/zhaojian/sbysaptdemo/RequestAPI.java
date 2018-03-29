package com.example.zhaojian.sbysaptdemo;

import com.example.annotation.METHOD;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaojian on 2018/3/29.
 */

public interface RequestAPI
{
    @METHOD("value1")
    List<Map<String,String>> test(int is, float it);
}

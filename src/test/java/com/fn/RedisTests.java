package com.fn;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
public class RedisTests {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * String
     */
    @Test
    public void testString() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("name", "zhangsan");
        valueOperations.get("name");
    }

    /**
     * Hash
     */
    @Test
    public void testHash() {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.put("001", "name", "zhangsan");
        hashOperations.put("001", "age", "19");

        String name = (String) hashOperations.get("001", "name");
        //获取所有key
        Set keys = hashOperations.keys("001");
        //获取所有value
        List values = hashOperations.values("001");
    }

    /**
     * List
     */
    @Test
    public void testList() {
        ListOperations listOperations = redisTemplate.opsForList();
        //left表示左边，即插入到列表头部
        listOperations.leftPush("list", "a");
        //一次性插入多个
        listOperations.leftPushAll("list", "a", "b", "c");
        //取值
        listOperations.range("list", 0, -1);
        //获取列表长度，对应命令LLEN key
        listOperations.size("list");
    }

    /**
     * Set
     */
    @Test
    public void testSet() {
        SetOperations setOperations = redisTemplate.opsForSet();
        //存值
        setOperations.add("set", "a", "b", "c", "d");
        //取值
        Set set = setOperations.members("set");
        //删除
        setOperations.remove("set", "a", "c");
    }

    /**
     * Sorted Set
     */
    @Test
    public void testSortedSet() {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        //存值
        zSetOperations.add("ss", "a", 10.0);
        zSetOperations.add("ss", "b", 9.0);
        zSetOperations.add("ss", "c", 8.0);
        //取值
        Set<String> ss = zSetOperations.range("ss", 0, -1);
        //修改分数,b+1
        zSetOperations.incrementScore("ss", "b", 1.0);
        //删除
        zSetOperations.remove("ss", "a", "b");
    }

    /**
     * 通用方法
     */
    @Test
    public void testCommon() {
        //查看所有Key
        redisTemplate.keys("*");
        //目标key是否存在
        redisTemplate.hasKey("keyName");
        //key值的类型
        redisTemplate.type("keyName");
        //key TTL时间
        redisTemplate.getExpire("keyName");
        //删除key
        redisTemplate.delete("keyName");
    }

}

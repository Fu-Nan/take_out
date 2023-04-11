package com.fn;

import com.fn.reggie.TakeOutApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = TakeOutApplication.class)
public class ReggieTakeOutApplicationTests {
}

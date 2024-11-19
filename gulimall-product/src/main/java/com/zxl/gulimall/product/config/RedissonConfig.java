package com.zxl.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：zxl
 * @Description: redisson配置类
 * @ClassName: RedissonConfig
 * @date ：2024/11/17 16:52
 */
@Configuration
public class RedissonConfig {
    /**
     * 所有对Redisson的使用都是通过RedissonClient对象
     * @return
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(){
        //1.创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://8.130.176.200:8009");
        //2.根据Config创建出RedissonClient实例
        return Redisson.create(config);
    }
}

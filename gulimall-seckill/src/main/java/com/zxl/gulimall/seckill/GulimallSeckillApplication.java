package com.zxl.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import javax.sql.DataSource;


/**
 *一、定时任务：自动配置类TaskSchedulingAutoConfiguration
 *		1、Spring中6位组成，不允许7位d的年
 *  	2、周的位置，1-7代表周一到周日
 * 		3、定时任务不应该阻塞。默认是阻塞的
 *      	 1）、可以让业务运行以异步的方式，自己提交到线程池
 *       	 2）、支持定时任务线程池；设置TaskSchedulingProperties;
 *             		spring.task.scheduling.pool.size=5
 *      	 3)、让定时任务异步执行
 *           		异步任务：
 *      解决：使用“异步任务”来完成定时任务不阻塞的功能
 * 二、异步任务：自动配置类TaskExecutionAutoConfiguration
 * 		1.@EnableAsync 开启异步任务功能
 * 		2.@Async 给希望异步任务的方法上标注
 *
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(value = {})
public class GulimallSeckillApplication {
	public static void main(String[] args) {
		SpringApplication.run(GulimallSeckillApplication.class, args);
	}

}

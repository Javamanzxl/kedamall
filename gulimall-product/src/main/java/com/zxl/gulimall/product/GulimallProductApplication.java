package com.zxl.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 1、整合MyBatis-Plus
 *      1）、导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.3.1</version>
 *      </dependency>
 *      2）、配置
 *          1、配置数据源；
 *              1）、导入数据库的驱动。https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-versions.html
 *              2）、在application.yml配置数据源相关信息
 *          2、配置MyBatis-Plus；
 *              1）、使用@MapperScan
 *              2）、告诉MyBatis-Plus，sql映射文件位置
 *
 * 2、逻辑删除
 *  1）、配置全局的逻辑删除规则（省略）
 *  2）、配置逻辑删除的组件Bean（省略）
 *  3）、给Bean加上逻辑删除注解@TableLogic
 *
 * 3、JSR303
 *   1）、给Bean添加校验注解:javax.validation.constraints，并定义自己的message提示
 *   2)、开启校验功能@Valid
 *      效果：校验错误以后会有默认的响应；
 *   3）、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
 *   4）、分组校验（多场景的复杂校验）
 *         1)、	@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
 *          给校验注解标注什么情况需要进行校验
 *         2）、@Validated({AddGroup.class})
 *         3)、默认没有指定分组的校验注解@NotBlank，在分组校验情况@Validated({AddGroup.class})下不生效，只会在@Validated生效；
 *
 *   5）、自定义校验
 *      1）、编写一个自定义的校验注解
 *      2）、编写一个自定义的校验器 ConstraintValidator
 *      3）、关联自定义的校验器和自定义的校验注解
 *
 * 4、统一的异常处理
 * @ControllerAdvice
 *  1）、编写异常处理类，使用@ControllerAdvice。
 *  2）、使用@ExceptionHandler标注方法可以处理的异常。
 *
 * 5.模板引擎
 *  1）、thymleaf-starter:关闭缓存
 *  2)、静态资源都放在static文件夹下就可以按照路径直接访问
 *  3）、页面放在templates下可以直接访问
 *      springboot访问项目的时候，默认会找index
 *  4)、页面修改不重启服务器实时更新
 *      1.引入dev-tools依赖
 *      2.修改完页面 ctrl shift f9重新自动编译下页面
 * 6.整合redisson作为分布式锁等功能的框架
 *  1）、引入依赖
 *  2）、配置redisson
 * 7.整合SpringCache简化缓存开发
 *  1）、引入依赖
 *  2）、写配置
 *      (1)、自动配置了哪些
 *          CacheAutoConfiguration会导入RedisCacheAutoConfiguration;
 *          RedisCacheAutoConfiguration自动配好了RedisCacheManager;
 *      (2)、配置使用redis作为缓存
 * 3）、测试使用缓存
 *      @Cacheable:触发将数据保存到缓存的操作
 *      @CacheEvict:从缓存删除
 *      @CachePut:不影响执行更新缓存
 *      @Caching:组合以上多个操作
 *      @CacheConfig:在类级别共享缓存的相同配置
 * 4）、开启缓存功能    @EnableCaching
 * 5）、CacheAutoConfiguration->RedisCacheConfiguration->
 *      自动化配置了RedisCacheManager->初始化所有缓存->每个缓存觉得使用什么配置
 *      ->如果redisCacheConfiguration有就用没有就用默认的->想改缓存的配置，只需要给容器中放一个
 *      RedisCacheConfiguration即可->就会应用到当前RedisCacheManager管理的所有缓存分区中
 */
//@MapperScan(basePackages = "com.zxl.gulimall.product.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zxl.gulimall.product.feign")
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}

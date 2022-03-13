package com.yuewen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @author wangshiyang
 * @since 2022/2/14
 **/
@SpringBootApplication
public class YuewenEsApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuewenEsApiApplication.class, args);
    }
}

package com.wujie.im;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wujie.im.mapper")
public class WujieImApplication {
    public static void main(String[] args) {
        SpringApplication.run(WujieImApplication.class, args);
    }
}

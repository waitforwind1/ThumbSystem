package com.usst.thumbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ThumbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThumbsApplication.class, args);
    }


}

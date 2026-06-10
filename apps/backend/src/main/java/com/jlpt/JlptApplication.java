/* (c) JLPT E-Learning Platform */
package com.jlpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.jlpt")
@EntityScan(basePackages = "com.jlpt")
@EnableJpaRepositories(basePackages = "com.jlpt")
public class JlptApplication {

    public static void main(String[] args) {
        SpringApplication.run(JlptApplication.class, args);
    }
}

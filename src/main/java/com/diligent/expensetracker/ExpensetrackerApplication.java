package com.diligent.expensetracker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class ExpensetrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpensetrackerApplication.class, args);
    }

    @Bean
    public CommandLineRunner listBeans(ApplicationContext ctx) {
        return args -> {
            Arrays.stream(ctx.getBeanDefinitionNames())
                    .filter(n -> n.toLowerCase().contains("mapper"))
                    .forEach(System.out::println);
        };
    }
}
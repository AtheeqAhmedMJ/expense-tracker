package com.diligent.expensetracker.config;

import com.diligent.expensetracker.mapper.ExpenseMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ExpenseMapper expenseMapper() {
        return Mappers.getMapper(ExpenseMapper.class);
    }
}
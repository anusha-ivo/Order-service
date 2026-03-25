package com.ordermanagement.order.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SqlQueryProvider {
    @Autowired
    private Environment environment;

    public String getQuery(String key) {
        return environment.getProperty(key);
    }

}

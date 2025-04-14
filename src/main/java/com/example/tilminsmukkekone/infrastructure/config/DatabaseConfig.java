package com.example.tilminsmukkekone.infrastructure.config;

import com.example.tilminsmukkekone.infrastructure.repositories.DatabaseOperations;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://176.9.89.36:3306/tilminesmukkekone?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        config.setUsername("newuser");
        config.setPassword("h$HWvLP@(D7u");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(3);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(1800000);

        // Performance settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }

    @Bean
    public DatabaseOperations databaseOperations(DataSource dataSource) {
        return new DatabaseOperations(dataSource);
    }
}
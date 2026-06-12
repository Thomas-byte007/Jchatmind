package com.kama.jchatmind.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * ?????? -- ?? Agent ??????
 *
 * ????(primaryDataSource):???? Mapper/???????,????????
 * ?????(restrictedDataSource):Agent ? DataBaseTools ??,?? + ?? DB ??
 *
 * ??? ProxyCredentialFetcher ??(????????????),JChatMind ???????
 */
@Configuration
public class DataSourceConfig {

    private final ProxyCredentialFetcher credentials;

    public DataSourceConfig(ProxyCredentialFetcher credentials) {
        this.credentials = credentials;
    }

    // ========== ????:??????(Mapper?????) ==========

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(credentials.getDbUrl());
        ds.setUsername(credentials.getDbUsername());
        ds.setPassword(credentials.getDbPassword());
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(10);
        ds.setPoolName("primary-pool");
        return ds;
    }

    @Bean
    @Primary
    public JdbcTemplate primaryJdbcTemplate() {
        return new JdbcTemplate(primaryDataSource());
    }

    // ========== ?????:Agent DataBaseTools ?? ==========

    @Bean("restrictedDataSource")
    public DataSource restrictedDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(credentials.getDbUrl());
        ds.setUsername(credentials.getRestrictedDbUsername());
        ds.setPassword(credentials.getRestrictedDbPassword());
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(3);
        ds.setReadOnly(true);
        ds.setPoolName("restricted-pool");
        return ds;
    }

    @Bean("restrictedJdbcTemplate")
    public JdbcTemplate restrictedJdbcTemplate() {
        return new JdbcTemplate(restrictedDataSource());
    }
}

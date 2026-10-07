package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.infrastructure.adapter.persistence.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class DataSourceSpringConfig {

  @Value("${spring.datasource.url:}")
  private String springDatasourceUrl;

  @Value("${spring.datasource.username:${db.username:postgres}}")
  private String datasourceUsername;

  @Value("${spring.datasource.password:${db.password:postgres}}")
  private String datasourcePassword;

  @Value("${spring.datasource.driver-class-name:}")
  private String driverClassName;

  @Value("${db.host:localhost}")
  private String dbHost;

  @Value("${db.port:5432}")
  private int dbPort;

  @Value("${db.name:users_db}")
  private String dbName;

  @Value("${db.type:postgresql}")
  private String dbType;

  @Bean
  public DataSource dataSource() {
    final HikariConfig hikariConfig = new HikariConfig();

    if (springDatasourceUrl != null && !springDatasourceUrl.isBlank()) {
      hikariConfig.setJdbcUrl(springDatasourceUrl);
      hikariConfig.setUsername(datasourceUsername);
      hikariConfig.setPassword(datasourcePassword);
      if (driverClassName != null && !driverClassName.isBlank()) {
        hikariConfig.setDriverClassName(driverClassName);
      }
      log.info("[DataSourceSpringConfig] DataSource inicializado via spring.datasource.url: {}", springDatasourceUrl);
    } else {
      final DatabaseConfig config =
          new DatabaseConfig(dbHost, dbPort, dbName, datasourceUsername, datasourcePassword, dbType);
      hikariConfig.setJdbcUrl(config.buildJdbcUrl());
      hikariConfig.setUsername(config.username());
      hikariConfig.setPassword(config.password());
      log.info("[DataSourceSpringConfig] DataSource inicializado via DatabaseConfig. type={} host={} port={}", dbType, dbHost, dbPort);
    }

    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(2);
    hikariConfig.setConnectionTimeout(30_000);

    return new HikariDataSource(hikariConfig);
  }
}

package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.infrastructure.adapter.persistence.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class DataSourceSpringConfig {

  private static final String PROP_DATABASE_URL = "${DATABASE_URL:${db.url:}}";
  private static final String PROP_DB_HOST      = "${db.host:localhost}";
  private static final String PROP_DB_PORT      = "${db.port:3306}";
  private static final String PROP_DB_NAME      = "${db.name:crud_usuarios}";
  private static final String PROP_DB_USERNAME  = "${db.username:root}";
  private static final String PROP_DB_PASSWORD  = "${db.password:}";
  private static final String PROP_DB_TYPE      = "${db.type:mysql}";

  @Value(PROP_DATABASE_URL)
  private String databaseUrl;

  @Value(PROP_DB_HOST)
  private String dbHost;

  @Value(PROP_DB_PORT)
  private int dbPort;

  @Value(PROP_DB_NAME)
  private String dbName;

  @Value(PROP_DB_USERNAME)
  private String dbUsername;

  @Value(PROP_DB_PASSWORD)
  private String dbPassword;

  @Value(PROP_DB_TYPE)
  private String dbType;

  @Bean
  public DataSource dataSource() {
    final HikariConfig hikariConfig = new HikariConfig();

    if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
      configureFromUrl(hikariConfig, databaseUrl.trim());
    } else {
      configureFromIndividualProps(hikariConfig);
    }

    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(2);
    hikariConfig.setConnectionTimeout(30_000);

    return new HikariDataSource(hikariConfig);
  }

  private void configureFromUrl(final HikariConfig hikariConfig, final String rawUrl) {
    try {
      log.info("[DataSourceSpringConfig] Configurando DataSource desde DATABASE_URL");
      // Manejar formato postgres:// o postgresql://
      String cleanUrl = rawUrl;
      if (cleanUrl.startsWith("postgres://")) {
        cleanUrl = "postgresql://" + cleanUrl.substring("postgres://".length());
      }
      final URI uri = new URI(cleanUrl);
      final String host = uri.getHost();
      final int port = uri.getPort() == -1 ? 5432 : uri.getPort();
      final String path = uri.getPath(); // Ej: /crud_usuarios_3ugt
      
      String user = null;
      String pass = null;
      if (uri.getUserInfo() != null) {
        final String[] credentials = uri.getUserInfo().split(":", 2);
        user = credentials[0];
        if (credentials.length > 1) {
          pass = credentials[1];
        }
      }

      final String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s", host, port, path);
      hikariConfig.setJdbcUrl(jdbcUrl);
      if (user != null) hikariConfig.setUsername(user);
      if (pass != null) hikariConfig.setPassword(pass);
      log.info("[DataSourceSpringConfig] Conectado exitosamente con URL a host={} port={}", host, port);
    } catch (final Exception e) {
      log.error("[DataSourceSpringConfig] Error parseando DATABASE_URL, aplicando como JDBC plana: {}", e.getMessage());
      hikariConfig.setJdbcUrl(rawUrl);
    }
  }

  private void configureFromIndividualProps(final HikariConfig hikariConfig) {
    final DatabaseConfig config =
        new DatabaseConfig(dbHost, dbPort, dbName, dbUsername, dbPassword, dbType);
    hikariConfig.setJdbcUrl(config.buildJdbcUrl());
    hikariConfig.setUsername(config.username());
    hikariConfig.setPassword(config.password());
    log.info("[DataSourceSpringConfig] DataSource inicializado. type={} host={} port={}", dbType, dbHost, dbPort);
  }
}

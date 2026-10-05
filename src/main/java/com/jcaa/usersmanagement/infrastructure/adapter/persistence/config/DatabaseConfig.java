package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

/**
 * Configuración de conexión a base de datos.
 * Soporta MySQL y PostgreSQL según el tipo indicado por {@code dbType}.
 * <p>
 * Para PostgreSQL en Render/Supabase se agrega {@code sslmode=require} automáticamente.
 */
public record DatabaseConfig(
    String host, int port, String databaseName, String username, String password, String dbType) {

  private static final String URL_MYSQL =
      "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

  // sslmode=require es obligatorio para Render y Supabase
  private static final String URL_POSTGRESQL =
      "jdbc:postgresql://%s:%d/%s?sslmode=require";

  public String buildJdbcUrl() {
    if ("postgresql".equalsIgnoreCase(dbType)) {
      return String.format(URL_POSTGRESQL, host, port, databaseName);
    }
    return String.format(URL_MYSQL, host, port, databaseName);
  }
}

package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

/**
 * Configuración de conexión a base de datos.
 * Soporta MySQL (local, Aiven y TiDB Cloud NewSQL Serverless) y PostgreSQL según {@code dbType}.
 * <p>
 * Para PostgreSQL se agrega {@code sslmode=require} automáticamente.
 * Para MySQL / TiDB Cloud se soporta {@code sslMode} configurable (por defecto {@code VERIFY_IDENTITY}).
 */
public record DatabaseConfig(
    String host, int port, String databaseName, String username, String password, String dbType, String sslMode) {

  public DatabaseConfig(String host, int port, String databaseName, String username, String password, String dbType) {
    this(host, port, databaseName, username, password, dbType, "VERIFY_IDENTITY");
  }

  public String buildJdbcUrl() {
    if ("postgresql".equalsIgnoreCase(dbType)) {
      return String.format("jdbc:postgresql://%s:%d/%s?sslmode=require", host, port, databaseName);
    }
    // Para TiDB Cloud y MySQL con TLS/SSL
    String sslParam = (sslMode != null && !sslMode.isBlank() && !"DISABLED".equalsIgnoreCase(sslMode) && !"false".equalsIgnoreCase(sslMode))
        ? ("sslMode=" + sslMode)
        : "useSSL=false";
    return String.format(
        "jdbc:mysql://%s:%d/%s?%s&serverTimezone=UTC&allowPublicKeyRetrieval=true",
        host, port, databaseName, sslParam);
  }
}

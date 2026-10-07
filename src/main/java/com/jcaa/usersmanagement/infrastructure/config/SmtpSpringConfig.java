package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.infrastructure.adapter.email.SmtpConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmtpSpringConfig {

  @Value("${spring.mail.host:${smtp.host:smtp.gmail.com}}")
  private String smtpHost;

  @Value("${spring.mail.port:${smtp.port:587}}")
  private int smtpPort;

  @Value("${spring.mail.username:${smtp.username:${MAIL_USERNAME:}}}")
  private String smtpUsername;

  @Value("${spring.mail.password:${smtp.password:${MAIL_PASSWORD:}}}")
  private String smtpPassword;

  @Value("${smtp.from.address:${spring.mail.username:${MAIL_USERNAME:admin@example.com}}}")
  private String smtpFromAddress;

  @Value("${smtp.from.name:${MAIL_FROM_NAME:Gestion de Usuarios}}")
  private String smtpFromName;

  @Bean
  public SmtpConfig smtpConfig() {
    return new SmtpConfig(smtpHost, smtpPort, smtpUsername, smtpPassword, smtpFromAddress, smtpFromName);
  }
}


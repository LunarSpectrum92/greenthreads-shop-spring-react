    package com.Konopka.eCommerce.configurations;

    import org.springframework.context.annotation.Bean;
    import org.springframework.boot.test.context.TestConfiguration;
    import org.testcontainers.containers.PostgreSQLContainer;
    import org.testcontainers.utility.DockerImageName;
    import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

    @TestConfiguration(proxyBeanMethods = false)
    public class TestConfigurations {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgreSQLContainer() {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
        }

    }
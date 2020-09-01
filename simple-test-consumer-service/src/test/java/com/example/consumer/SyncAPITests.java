package com.example.consumer;

import com.example.consumer.controller.SyncAPI;
import com.example.consumer.entity.User;
import com.example.consumer.entity.UserCreateResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class SyncAPITests {

    @Autowired
    private SyncAPI controller;

    private static final long memoryInBytes = 8 * 1024 * 1024;
    private static final long memorySwapInBytes = 12 * 1024 * 1024;

    @Container
    private static final JdbcDatabaseContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.2"))
            .withPassword("postgres")
            .withUsername("postgres")
            .withDatabaseName("users")
            .withInitScript("data/init.sql")
            // resource configuration should be moved to test scenarios
            .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig())
                    .withCpuCount(1L)
                    .withCpuPercent(20L)
                    .withMemory(memoryInBytes)
                    .withMemorySwap(memorySwapInBytes)
            );

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        org.testcontainers.Testcontainers.exposeHostPorts(5432);
//        DockerComposeContainer

        postgres.start();
        postgres.start();

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void testCreateGetUser() {
        UserCreateResponse response = controller.create(
                new User("", "Serhii", "serhii@mail.com"));
        assertThat(response.getId(), not(is(emptyString())));

        User user = controller.get(response.getId());
        assertThat(user.getName(), equalTo("Serhii"));
    }

    @Test
    void testCreateDuplicateEmailThrows() {
        controller.create(new User("", "Serhii2", "serhii2@mail.com"));
        assertThrows(DuplicateKeyException.class, () ->
                controller.create(new User("", "Serhii2", "serhii2@mail.com")));
    }

}

package ru.shelf.config;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@Profile("grafana")
class GrafanaSecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain grafanaActuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher(EndpointRequest.to("health", "prometheus"))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}

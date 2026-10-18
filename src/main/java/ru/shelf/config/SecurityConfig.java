package ru.shelf.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URI;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {
    @Bean
    ClientRegistrationRepository clientRegistrations(AuthProperties properties) {
        // Explicit endpoints avoid a network dependency at startup. The issuer and
        // signature are still validated by Spring Security when an ID token arrives.
        String issuer = properties.issuerUri().replaceAll("/$", "");
        String endpoint = issuer + "/protocol/openid-connect";
        var registration =
            ClientRegistration.withRegistrationId("keycloak")
                .clientId(properties.clientId())
                .clientSecret(properties.clientSecret())
                .clientName("Аккаунт Полки")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .issuerUri(issuer)
                .authorizationUri(endpoint + "/auth")
                .tokenUri(endpoint + "/token")
                .jwkSetUri(endpoint + "/certs")
                .userInfoUri(endpoint + "/userinfo")
                .userNameAttributeName("sub")
                .providerConfigurationMetadata(
                    Map.of("end_session_endpoint", endpoint + "/logout"))
                .build();
        return new InMemoryClientRegistrationRepository(registration);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http, ClientRegistrationRepository clients, AuthProperties properties)
        throws Exception {
        var resolver =
            new DefaultOAuth2AuthorizationRequestResolver(clients, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(
            OAuth2AuthorizationRequestCustomizers.withPkce());
        var logout = new OidcClientInitiatedLogoutSuccessHandler(clients);
        logout.setPostLogoutRedirectUri(properties.postLogoutRedirectUri());
        URI issuer = URI.create(properties.issuerUri());
        String identityOrigin = issuer.getScheme() + "://" + issuer.getRawAuthority();
        http.authorizeHttpRequests(
                auth ->
                    auth.requestMatchers(
                            "/",
                            "/login",
                            "/css/**",
                            "/js/**",
                            "/favicon.svg",
                            "/icons.svg",
                            "/error")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
            .oauth2Login(
                oauth ->
                    oauth.loginPage("/login")
                        .authorizationEndpoint(
                            endpoint ->
                                endpoint.authorizationRequestResolver(
                                    resolver))
                        .defaultSuccessUrl("/shelf", true)
                        .failureUrl("/login?error"))
            .logout(
                config ->
                    config.logoutSuccessHandler(logout)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("SHELF_SESSION"))
            .headers(
                headers ->
                    headers.contentSecurityPolicy(
                        csp ->
                            csp.policyDirectives(
                                "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; "
                                    + "font-src 'self'; object-src 'none'; frame-ancestors 'none'; base-uri 'self'; form-action 'self' "
                                    + identityOrigin)));
        return http.build();
    }
}

package ru.shelf.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;
import ru.shelf.repository.ShelfItemRepository;
import ru.shelf.service.ShelfService;
import ru.shelf.service.dto.ItemCommand;
import ru.shelf.service.dto.ShelfItemData;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class WebIntegrationTest {
    protected static final String ISSUER = "http://localhost:8180/realms/shelf";
    @Autowired MockMvc mvc;
    @Autowired ShelfItemRepository repository;
    @Autowired ShelfService service;
    @Autowired ClientRegistrationRepository clients;

    @BeforeEach
    void clearData() {
        repository.deleteAll();
    }

    protected OidcLoginRequestPostProcessor as(String subject) {
        return oidcLogin()
                .clientRegistration(clients.findByRegistrationId("keycloak"))
                .idToken(
                        token ->
                                token.subject(subject)
                                        .issuer(ISSUER)
                                        .claim("name", "Читатель " + subject)
                                        .claim("email", subject + "@example.test"));
    }

    protected ShelfItemData saved(
            String subject, String title, Category category, ItemStatus status) {
        return service.create(ISSUER + "|" + subject, new ItemCommand(title, category, status, ""));
    }
}

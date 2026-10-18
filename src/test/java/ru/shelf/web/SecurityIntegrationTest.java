package ru.shelf.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;

class SecurityIntegrationTest extends WebIntegrationTest {
    @Test
    void guestCanSeeHomeLoginAndAssets() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("На одной полке")));
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Войти в аккаунт")));
        for (String path :
                new String[] {"/css/app.css", "/js/app.js", "/icons.svg", "/favicon.svg"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    void privatePagesRequireLogin() throws Exception {
        for (String path : new String[] {"/shelf", "/profile", "/items/new", "/items/1/edit"}) {
            mvc.perform(get(path))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    @Test
    void oauthRedirectUsesCodeStateNonceAndPkce() throws Exception {
        mvc.perform(get("/oauth2/authorization/keycloak"))
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        header().string(
                                        "Location",
                                        allOf(
                                                startsWith(
                                                        ISSUER + "/protocol/openid-connect/auth?"),
                                                containsString("response_type=code"),
                                                containsString("state="),
                                                containsString("nonce="),
                                                containsString("code_challenge="),
                                                containsString("code_challenge_method=S256"),
                                                containsString(
                                                        "redirect_uri=http://localhost/login/oauth2/code/keycloak"))));
    }

    @Test
    void invalidOauthCallbackDoesNotAuthenticate() throws Exception {
        mvc.perform(get("/login/oauth2/code/keycloak").param("code", "fake").param("state", "fake"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void signedInHomeRedirectsToShelf() throws Exception {
        mvc.perform(get("/").with(as("alice"))).andExpect(redirectedUrl("/shelf"));
    }

    @Test
    void mutationsRequireCsrf() throws Exception {
        var item = saved("alice", "Дюна", Category.BOOK, ItemStatus.PLANNED);
        for (String path :
                new String[] {
                    "/items", "/items/" + item.id(), "/items/" + item.id() + "/delete", "/logout"
                }) {
            mvc.perform(post(path).with(as("alice"))).andExpect(status().isForbidden());
        }
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void responseHasSecurityHeaders() throws Exception {
        mvc.perform(get("/"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(
                        header().string(
                                        "Content-Security-Policy",
                                        containsString("frame-ancestors 'none'")))
                .andExpect(header().string("Cache-Control", containsString("no-store")));
    }

    @Test
    void logoutRedirectsToIdentityProvider() throws Exception {
        mvc.perform(post("/logout").with(as("alice")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        header().string(
                                        "Location",
                                        startsWith(ISSUER + "/protocol/openid-connect/logout?")));
    }
}

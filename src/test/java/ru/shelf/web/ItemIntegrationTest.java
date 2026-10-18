package ru.shelf.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;
import ru.shelf.service.dto.ItemCommand;
import ru.shelf.service.dto.ShelfFilter;

class ItemIntegrationTest extends WebIntegrationTest {
    @Test
    void newFormIncludesCsrfToken() throws Exception {
        mvc.perform(get("/items/new").with(as("alice")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void createStoresRecordForAuthenticatedOwner() throws Exception {
        mvc.perform(
                        post("/items")
                                .with(as("alice"))
                                .with(csrf())
                                .param("title", "  Дюна  ")
                                .param("category", "BOOK")
                                .param("status", "PLANNED")
                                .param("note", "Прочитать летом")
                                .param("ownerId", ISSUER + "|bob")
                                .param("id", "123"))
                .andExpect(redirectedUrl("/shelf"));
        assertThat(service.find(ISSUER + "|alice", ShelfFilter.none()).items())
                .singleElement()
                .satisfies(item -> assertThat(item.title()).isEqualTo("Дюна"));
        assertThat(service.find(ISSUER + "|bob", ShelfFilter.none()).items()).isEmpty();
    }

    @Test
    void invalidFormIsNotSaved() throws Exception {
        mvc.perform(
                        post("/items")
                                .with(as("alice"))
                                .with(csrf())
                                .param("title", "   ")
                                .param("category", "BOOK")
                                .param("status", "PLANNED"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("item", "title"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void oversizedTitleAndNoteAreRejected() throws Exception {
        mvc.perform(
                        post("/items")
                                .with(as("alice"))
                                .with(csrf())
                                .param("title", "x".repeat(161))
                                .param("note", "x".repeat(2001))
                                .param("category", "BOOK")
                                .param("status", "PLANNED"))
                .andExpect(model().attributeHasFieldErrors("item", "title", "note"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void invalidCategoryIsRejected() throws Exception {
        mvc.perform(
                        post("/items")
                                .with(as("alice"))
                                .with(csrf())
                                .param("title", "Дюна")
                                .param("category", "UNKNOWN")
                                .param("status", "PLANNED"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("item", "category"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void ownerCanEditAndDeleteRecord() throws Exception {
        var item = saved("alice", "Дюна", Category.BOOK, ItemStatus.PLANNED);
        mvc.perform(get("/items/" + item.id() + "/edit").with(as("alice")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Дюна")));
        mvc.perform(
                        post("/items/" + item.id())
                                .with(as("alice"))
                                .with(csrf())
                                .param("title", "Дюна: прочитано")
                                .param("category", "BOOK")
                                .param("status", "DONE")
                                .param("note", "Отличная книга"))
                .andExpect(redirectedUrl("/shelf"));
        assertThat(service.get(ISSUER + "|alice", item.id()).status()).isEqualTo(ItemStatus.DONE);
        mvc.perform(post("/items/" + item.id() + "/delete").with(as("alice")).with(csrf()))
                .andExpect(redirectedUrl("/shelf"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void otherUserCannotReadEditOrDeleteRecord() throws Exception {
        var item = saved("alice", "Дюна", Category.BOOK, ItemStatus.PLANNED);
        String path = "/items/" + item.id();
        mvc.perform(get(path + "/edit").with(as("bob"))).andExpect(status().isNotFound());
        mvc.perform(
                        post(path)
                                .with(as("bob"))
                                .with(csrf())
                                .param("title", "Взлом")
                                .param("category", "BOOK")
                                .param("status", "DONE"))
                .andExpect(status().isNotFound());
        mvc.perform(post(path + "/delete").with(as("bob")).with(csrf()))
                .andExpect(status().isNotFound());
        assertThat(service.get(ISSUER + "|alice", item.id()).title()).isEqualTo("Дюна");
    }

    @Test
    void userContentIsEscaped() throws Exception {
        service.create(
                ISSUER + "|alice",
                new ItemCommand(
                        "<script>alert(1)</script>",
                        Category.BOOK,
                        ItemStatus.PLANNED,
                        "<img src=x onerror=alert(1)>"));
        mvc.perform(get("/shelf").with(as("alice")))
                .andExpect(status().isOk())
                .andExpect(
                        content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(not(containsString("<img src=x onerror=alert(1)>"))));
    }

    @Test
    void unknownRecordAndBadFilterAreHandled() throws Exception {
        mvc.perform(get("/items/999999/edit").with(as("alice"))).andExpect(status().isNotFound());
        mvc.perform(get("/shelf").param("category", "INVALID").with(as("alice")))
                .andExpect(status().isBadRequest());
    }
}

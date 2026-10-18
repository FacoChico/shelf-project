package ru.shelf.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;

class ShelfPageIntegrationTest extends WebIntegrationTest {
    @Test
    void emptyShelfAndProfileRender() throws Exception {
        mvc.perform(get("/shelf").with(as("alice")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Первая история")));
        mvc.perform(get("/profile").with(as("alice")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alice@example.test")));
    }

    @Test
    void shelfHidesOtherUsersRecordsAndSupportsFilters() throws Exception {
        saved("alice", "Дюна", Category.BOOK, ItemStatus.PLANNED);
        saved("alice", "Прибытие", Category.MOVIE, ItemStatus.DONE);
        saved("bob", "Секрет Боба", Category.GAME, ItemStatus.IN_PROGRESS);
        mvc.perform(get("/shelf").with(as("alice")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Секрет Боба"))));
        mvc.perform(
                        get("/shelf")
                                .with(as("alice"))
                                .param("q", "ДЮНА")
                                .param("category", "BOOK")
                                .param("status", "PLANNED"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", hasSize(1)))
                .andExpect(content().string(containsString("Дюна")))
                .andExpect(content().string(not(containsString("Прибытие"))));
        mvc.perform(get("/shelf").with(as("alice")).param("q", "нет такой записи"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ничего не нашлось")));
    }
}

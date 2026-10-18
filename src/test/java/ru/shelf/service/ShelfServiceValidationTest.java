package ru.shelf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;
import ru.shelf.service.dto.ItemCommand;
import ru.shelf.service.dto.ShelfFilter;

@SpringBootTest
@ActiveProfiles("test")
class ShelfServiceValidationTest {
    @Autowired ShelfService service;

    @Test
    void rejectsInvalidCommandWithoutAnHttpRequest() {
        String ownerId = "service-validation-owner";
        ItemCommand command = new ItemCommand(" ", Category.BOOK, ItemStatus.PLANNED, "");
        assertThatThrownBy(() -> service.create(ownerId, command))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(service.find(ownerId, ShelfFilter.none()).items()).isEmpty();
    }
}

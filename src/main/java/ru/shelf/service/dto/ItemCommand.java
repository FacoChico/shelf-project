package ru.shelf.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;

public record ItemCommand(
    @NotBlank @Size(max = 160) String title,
    @NotNull Category category,
    @NotNull ItemStatus status,
    @Size(max = 2000) String note
) {
}

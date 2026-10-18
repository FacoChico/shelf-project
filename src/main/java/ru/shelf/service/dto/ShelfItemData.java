package ru.shelf.service.dto;

import java.time.Instant;

import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;

public record ShelfItemData(
    Long id,
    String title,
    Category category,
    ItemStatus status,
    String note,
    Instant createdAt,
    Instant updatedAt
) {
}

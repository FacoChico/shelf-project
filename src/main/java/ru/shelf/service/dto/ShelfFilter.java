package ru.shelf.service.dto;

import java.util.Locale;

import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;

public record ShelfFilter(Category category, ItemStatus status, String query) {
    public ShelfFilter {
        query = query == null
            ? ""
            : query.strip().toLowerCase(Locale.ROOT);
    }

    public static ShelfFilter none() {
        return new ShelfFilter(null, null, "");
    }
}

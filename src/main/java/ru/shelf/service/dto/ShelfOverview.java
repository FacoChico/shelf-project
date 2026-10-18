package ru.shelf.service.dto;

import java.util.List;

public record ShelfOverview(List<ShelfItemData> items, ShelfStats stats) {

    public ShelfOverview {
        items = List.copyOf(items);
    }
}

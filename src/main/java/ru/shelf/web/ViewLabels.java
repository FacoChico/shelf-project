package ru.shelf.web;

import org.springframework.stereotype.Component;
import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;

@Component
public class ViewLabels {

    public String category(Category category) {
        return switch (category) {
            case BOOK -> "Книга";
            case MOVIE -> "Фильм";
            case GAME -> "Игра";
        };
    }

    public String categoryPlural(Category category) {
        return switch (category) {
            case BOOK -> "Книги";
            case MOVIE -> "Фильмы";
            case GAME -> "Игры";
        };
    }

    public String status(ItemStatus status) {
        return switch (status) {
            case PLANNED -> "В планах";
            case IN_PROGRESS -> "В процессе";
            case DONE -> "Завершено";
        };
    }
}

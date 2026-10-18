package ru.shelf.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.shelf.service.ItemNotFoundException;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String itemNotFound(Model model) {
        model.addAttribute("code", 404);
        model.addAttribute("heading", "На этой полке пусто");
        model.addAttribute("explanation", "Запись не найдена или недоступна твоему аккаунту.");
        return "error";
    }
}

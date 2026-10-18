package ru.shelf.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorsController implements ErrorController {

    @RequestMapping("/error")
    String error(HttpServletRequest request, Model model) {
        Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = code instanceof Integer value
            ? value
            : 500;
        model.addAttribute("code", status);
        model.addAttribute(
            "heading",
            switch (status) {
                case 404 -> "На этой полке пусто";
                case 403 -> "Действие недоступно";
                case 400 -> "Не удалось понять запрос";
                default -> "Что-то пошло не по плану";
            });
        model.addAttribute(
            "explanation",
            switch (status) {
                case 404 -> "Запись не найдена или недоступна твоему аккаунту.";
                case 403 -> "Обнови страницу и попробуй ещё раз. Возможно, время сеанса истекло.";
                case 400 -> "Проверь адрес страницы и параметры запроса.";
                default -> "Попробуй вернуться на полку и повторить действие чуть позже.";
            });
        return "error";
    }
}

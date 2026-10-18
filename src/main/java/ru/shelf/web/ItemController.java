package ru.shelf.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.shelf.service.ShelfService;
import ru.shelf.web.form.ItemForm;

@Controller
public class ItemController {
    private final ShelfService shelfService;

    public ItemController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @InitBinder("item")
    void bindItem(WebDataBinder binder) {
        binder.setAllowedFields("title", "category", "status", "note");
    }

    @GetMapping("/items/new")
    String newItem(Model model) {
        model.addAttribute("item", new ItemForm());
        return form(model, null);
    }

    @PostMapping("/items")
    String create(
        @AuthenticationPrincipal OidcUser user,
        @Valid @ModelAttribute("item") ItemForm item,
        BindingResult errors,
        Model model,
        RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return form(model, null);
        }
        shelfService.create(CurrentUser.ownerId(user), item.toCommand());
        flash.addFlashAttribute("notice", "Запись добавлена в коллекцию");
        return "redirect:/shelf";
    }

    @GetMapping("/items/{id}/edit")
    String edit(@AuthenticationPrincipal OidcUser user, @PathVariable Long id, Model model) {
        model.addAttribute("item", ItemForm.from(shelfService.get(CurrentUser.ownerId(user), id)));
        return form(model, id);
    }

    @PostMapping("/items/{id}")
    String update(
        @AuthenticationPrincipal OidcUser user,
        @PathVariable Long id,
        @Valid @ModelAttribute("item") ItemForm item,
        BindingResult errors,
        Model model,
        RedirectAttributes flash) {
        if (errors.hasErrors()) {
            shelfService.get(CurrentUser.ownerId(user), id);
            return form(model, id);
        }
        shelfService.update(CurrentUser.ownerId(user), id, item.toCommand());
        flash.addFlashAttribute("notice", "Изменения сохранены");
        return "redirect:/shelf";
    }

    @PostMapping("/items/{id}/delete")
    String delete(
        @AuthenticationPrincipal OidcUser user,
        @PathVariable Long id,
        RedirectAttributes flash) {
        shelfService.delete(CurrentUser.ownerId(user), id);
        flash.addFlashAttribute("notice", "Запись удалена с полки");
        return "redirect:/shelf";
    }

    private String form(Model model, Long id) {
        model.addAttribute("editing", id != null);
        model.addAttribute("itemId", id);
        model.addAttribute("activePage", "shelf");
        return "item-form";
    }
}

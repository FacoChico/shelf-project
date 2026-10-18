package ru.shelf.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;

@ControllerAdvice
public class CommonModelAttributes {

    @ModelAttribute
    void addAttributes(@AuthenticationPrincipal OidcUser user, Model model) {
        model.addAttribute("displayName", user == null ? "Гость" : CurrentUser.displayName(user));
        model.addAttribute("categories", Category.values());
        model.addAttribute("statuses", ItemStatus.values());
    }
}

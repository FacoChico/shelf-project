package ru.shelf.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.shelf.service.ShelfService;

@Controller
public class ProfileController {
    private final ShelfService shelfService;

    public ProfileController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @GetMapping("/profile")
    String profile(@AuthenticationPrincipal OidcUser user, Model model) {
        model.addAttribute("email", user.getEmail());
        model.addAttribute("stats", shelfService.stats(CurrentUser.ownerId(user)));
        model.addAttribute("activePage", "profile");
        return "profile";
    }
}

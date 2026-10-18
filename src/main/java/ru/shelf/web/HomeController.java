package ru.shelf.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    String home(@AuthenticationPrincipal OidcUser user) {
        return user == null ? "home" : "redirect:/shelf";
    }

    @GetMapping("/login")
    String login(@AuthenticationPrincipal OidcUser user) {
        return user == null ? "login" : "redirect:/shelf";
    }
}

package ru.shelf.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/")
    String home(@AuthenticationPrincipal OidcUser user) {
        LOG.debug("Shelf home page requested");
        return user == null ? "home" : "redirect:/shelf";
    }

    @GetMapping("/login")
    String login(@AuthenticationPrincipal OidcUser user) {
        return user == null ? "login" : "redirect:/shelf";
    }
}

package ru.shelf.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;
import ru.shelf.service.ShelfService;
import ru.shelf.service.dto.ShelfFilter;
import ru.shelf.service.dto.ShelfOverview;

@Controller
public class ShelfController {
    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @GetMapping("/shelf")
    String list(@AuthenticationPrincipal OidcUser user,
                @RequestParam(required = false) Category category,
                @RequestParam(required = false) ItemStatus status,
                @RequestParam(defaultValue = "") String q,
                Model model) {
        ShelfOverview overview =
            shelfService.find(CurrentUser.ownerId(user), new ShelfFilter(category, status, q));
        model.addAttribute("stats", overview.stats());
        model.addAttribute("items", overview.items());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("query", q);
        model.addAttribute("hasFilters", category != null || status != null || !q.isBlank());
        model.addAttribute("activePage", "shelf");
        return "shelf";
    }
}

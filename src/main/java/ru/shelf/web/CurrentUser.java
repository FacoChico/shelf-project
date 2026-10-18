package ru.shelf.web;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static String ownerId(OidcUser user) {
        return user.getIssuer().toString() + "|" + user.getSubject();
    }

    public static String displayName(OidcUser user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        if (user.getPreferredUsername() != null) return user.getPreferredUsername();
        return "Читатель";
    }
}

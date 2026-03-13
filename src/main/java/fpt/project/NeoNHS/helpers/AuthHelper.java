package fpt.project.NeoNHS.helpers;

import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
public final class AuthHelper {

    private AuthHelper() {}

    /**
     * NOTE: THIS METHOD THROW IF USER IS NOT AUTHENTICATED.
     * @return
     */
    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        if (!(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new UnauthorizedException("Invalid authenticated principal");
        }

        return userPrincipal;
    }

    /**
     * This method will return null if user not found
     * @return
     */
    public static UserPrincipal getCurrentUserPrincipalSilent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            log.info("User is not authenticated");
            return null;
        }

        if (!(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            log.info("Invalid authenticated principal: {}", auth.getPrincipal());
            return null;
        }

        return userPrincipal;
    }

    public static UUID getCurrentUserId() {
        return getCurrentUserPrincipal().getId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentUserPrincipal().getEmail();
    }
}

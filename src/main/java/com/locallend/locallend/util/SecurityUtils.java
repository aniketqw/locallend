package com.locallend.locallend.util;

import com.locallend.locallend.exception.BusinessException;
import com.locallend.locallend.security.MongoUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for extracting authenticated user information from SecurityContext.
 */
public class SecurityUtils {

    /**
     * Get current authenticated user's ID from JWT token in SecurityContext.
     * @return User ID
     * @throws BusinessException if user is not authenticated
     */
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MongoUserPrincipal principal) {
            return principal.getId();
        }
        throw new BusinessException("User not authenticated");
    }

    /**
     * Get current authenticated user's username from JWT token in SecurityContext.
     * @return Username
     * @throws BusinessException if user is not authenticated
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MongoUserPrincipal principal) {
            return principal.getUsername();
        }
        throw new BusinessException("User not authenticated");
    }

    /**
     * Get current authenticated user's principal.
     * @return MongoUserPrincipal
     * @throws BusinessException if user is not authenticated
     */
    public static MongoUserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MongoUserPrincipal principal) {
            return principal;
        }
        throw new BusinessException("User not authenticated");
    }
}

package com.locallend.locallend.security;

import com.locallend.locallend.model.User;
import com.locallend.locallend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService that loads users from MongoDB.
 * Supports authentication by username or email.
 */
@Service
public class MongoUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MongoUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load user by username or email.
     * @param username Username or email
     * @return MongoUserPrincipal wrapping the user
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public MongoUserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)));
        return new MongoUserPrincipal(user);
    }
}

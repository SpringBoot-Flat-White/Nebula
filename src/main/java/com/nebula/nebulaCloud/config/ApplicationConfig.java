package com.nebula.nebulaCloud.config;

import com.nebula.nebulaCloud.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for application-level beans, particularly for security.
 * This class centralizes the definition of core components for the authentication mechanism.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Defines the UserDetailsService bean.
     * This service acts as the bridge to our user data store. Spring Security uses it
     * to load user details by username (in our case, email).
     *
     * @param userRepository The repository to fetch user data. Spring injects this bean automatically.
     * @return An implementation of UserDetailsService that fetches users from the database.
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    /**
     * Defines the PasswordEncoder bean.
     * We use BCrypt, a strong hashing algorithm, for password security.
     *
     * @return An instance of BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the primary AuthenticationProvider bean.
     * While Spring Boot can auto-configure a DaoAuthenticationProvider, defining it
     * explicitly provides clarity, control, and is a best practice for non-trivial applications.
     * This method uses a modern dependency injection style by taking other beans as parameters.
     *
     * @param userDetailsService The service for loading user data.
     * @param passwordEncoder The encoder for password verification.
     * @return A fully configured DaoAuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Although the setters are marked as deprecated, this is the standard way to configure
        // the provider when defining it explicitly.
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Defines the AuthenticationManager bean.
     * This is the main interface for authentication processing. We expose it as a bean so it
     * can be injected into our authentication controllers.
     *
     * @param config The AuthenticationConfiguration from Spring's autoconfiguration.
     * @return The configured AuthenticationManager instance.
     * @throws Exception if an error occurs while retrieving the manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
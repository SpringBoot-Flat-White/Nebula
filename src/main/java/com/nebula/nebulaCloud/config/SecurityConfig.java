package com.nebula.nebulaCloud.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main security configuration class for the application.
 *
 * This class enables Spring's web security support and provides the central
 * configuration for the security filter chain. It adopts the modern, component-based
 * approach by defining a SecurityFilterChain bean, which replaces the deprecated
 * WebSecurityConfigurerAdapter.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final com.nebula.nebulaCloud.service.OAuth2UserService oAuth2UserService;

    /**
     * Defines the security filter chain that applies to all HTTP requests.
     * This is the central place to configure web-based security rules.
     *
     * @param http The HttpSecurity object to be configured. It allows customizing
     *             security settings for specific HTTP requests.
     * @return A SecurityFilterChain bean that Spring Security will use to protect the application.
     * @throws Exception if an error occurs during the configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (Cross-Site Request Forgery) protection.
                // This is standard for stateless REST APIs where authentication is not cookie-based.
                .csrf(csrf -> csrf.disable())

                // 2. Configure authorization rules for HTTP requests.
                .authorizeHttpRequests(auth -> auth
                        // Allow all OPTIONS requests (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Define public endpoints that do not require authentication.
                        // It is a common practice to make auth-related endpoints (login, register) public.
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/complete-profile", "/api/v1/auth/oauth2-test").permitAll()
                        .requestMatchers("/api/organizations/**","/api/individuals/**", "/v3/api-docs/**", "/swagger-ui/**","/swagger-ui.html").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        // Mercado Pago webhook endpoint (public for notifications)
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/webhook").permitAll()
                        // Mercado Pago return URLs (public for redirection)
                        .requestMatchers("/api/v1/payments/success", "/api/v1/payments/failure", "/api/v1/payments/pending").permitAll()
                        // Authenticated endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        // All other requests must be authenticated.
                        .anyRequest().authenticated()
                )

                // 2.5. Configure OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )

                // 3. Configure session management to be stateless.
                // Since we are using JWTs for authentication, we don't need Spring Security
                // to create or manage any HttpSession. This is crucial for a stateless architecture.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Set the custom authentication provider.
                // This tells Spring Security to use the provider we configured in ApplicationConfig,
                // which in turn uses our UserDetailsService and PasswordEncoder.
                .authenticationProvider(authenticationProvider)

                // 5. Add the custom JWT filter before the standard UsernamePasswordAuthenticationFilter.
                // This ensures that our JWT validation logic is executed for every relevant request
                // before Spring attempts any form-based authentication.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
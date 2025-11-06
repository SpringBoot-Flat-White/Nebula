package com.nebula.nebulaCloud.config;

import com.nebula.nebulaCloud.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A custom security filter that intercepts every HTTP request to process JWT-based authentication.
 *
 * This filter is responsible for:
 * 1. Extracting the JWT from the 'Authorization' header.
 * 2. Validating the token using the JwtService.
 * 3. Loading user details from the UserDetailsService.
 * 4. If the token is valid, setting the user's authentication details in the
 *    Spring Security context, effectively authenticating the user for the duration of the request.
 *
 * It extends OncePerRequestFilter to ensure it's executed only once per request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * The core logic of the filter. This method is called by the servlet container for each request.
     *
     * @param request     The incoming HttpServletRequest.
     * @param response    The outgoing HttpServletResponse.
     * @param filterChain The filter chain, which allows the request to proceed to the next filter.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException      if an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Check for the presence of the JWT and the 'Bearer ' prefix.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // If the header is missing or malformed, pass the request to the next filter
            // without setting the authentication context.
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract the JWT from the header.
        final String jwt = authHeader.substring(7); // "Bearer ".length() is 7

        // 3. Extract the user's email from the token.
        final String userEmail = jwtService.extractUsername(jwt);

        // 4. Check if the user is already authenticated for this request.
        // If the email is present and there's no authentication in the security context,
        // it means we are processing a new authentication for this request.
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details from the database using the email from the token.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Validate the token against the user details.
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // If the token is valid, create an authentication token.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credentials (password) are not needed as we are using a pre-authenticated token.
                        userDetails.getAuthorities()
                );
                // Enhance the authentication token with details from the web request (e.g., IP address).
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 6. Update the SecurityContextHolder with the new authentication token.
                // From this point on, the user is considered authenticated for this request.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 7. Pass the request and response to the next filter in the chain.
        filterChain.doFilter(request, response);
    }
}
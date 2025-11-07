package com.nebula.nebulaCloud.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A utility service for handling JSON Web Tokens (JWTs).
 *
 * This service encapsulates all logic related to JWTs, including their creation
 * (generation), parsing, and validation. It is a critical component for securing
 * the application's API by managing token-based authentication.
 */
@Service
public class JwtService {

    /**
     * The secret key used for signing and verifying JWTs. This key should be
     * a long, complex, Base64-encoded string and must be kept secure.
     * It is injected from the application's properties file.
     */
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * The expiration time for the JWT in milliseconds.
     * Injected from the application's properties file.
     */
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extracts the username (subject) from a given JWT.
     *
     * @param token The JWT from which to extract the username.
     * @return The username contained within the token's subject claim.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * A generic method to extract a specific claim from a JWT.
     * This allows for flexible retrieval of any piece of information stored in the token's payload.
     *
     * @param token          The JWT to parse.
     * @param claimsResolver A function that specifies how to extract the desired claim from the Claims object.
     * @param <T>            The type of the claim to be returned.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a new JWT for a given user without extra claims.
     *
     * @param userDetails The UserDetails object representing the user for whom the token is being created.
     * @return A newly generated, signed JWT string.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a new JWT with extra claims for a given user.
     * Extra claims can be used to store additional, non-sensitive user information like roles or permissions.
     *
     * @param extraClaims Additional claims to include in the token's payload.
     * @param userDetails The UserDetails object representing the user.
     * @return A newly generated, signed JWT string.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a JWT. The validation checks two things:
     * 1. The username in the token matches the username of the UserDetails object.
     * 2. The token has not expired.
     *
     * @param token       The JWT to validate.
     * @param userDetails The user details to validate the token against.
     * @return true if the token is valid for the given user, false otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if a JWT has expired by comparing its expiration claim with the current time.
     *
     * @param token The JWT to check.
     * @return true if the token is expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT's claims.
     *
     * @param token The JWT from which to extract the expiration date.
     * @return The expiration date.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses the JWT and returns all of its claims (the payload).
     * This method verifies the token's signature using the secret key.
     *
     * @param token The JWT to parse.
     * @return A Claims object containing all data from the token's payload.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Decodes the Base64 secret key and returns it as a {@link Key} object
     * suitable for use with the HMAC-SHA algorithm.
     *
     * @return The signing key for JWT verification and creation.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link User} entities.
 *
 * This interface provides the mechanism for storage, retrieval, update,
 * and delete operations on User objects. It leverages the power of
 * Spring Data JPA to automatically generate the necessary queries from
 * method names. By extending JpaRepository, we get a full set of
 * generic CRUD operations out of the box.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * Spring Data JPA will automatically generate a query for this method
     * based on its name: "findBy" + "Email". This is a core part of the
     * authentication process, as we need to load user details by their username (email).
     *
     * @param email The email address of the user to find. Must not be null.
     * @return An {@link Optional} containing the found user, or an empty
     *         {@link Optional} if no user with the given email exists.
     *         Using Optional is a best practice to avoid NullPointerExceptions.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their OAuth2 provider and provider ID.
     *
     * This method is used to check if a user has already registered via OAuth2
     * (Google, GitHub, etc.) based on the provider name and the unique ID from that provider.
     *
     * @param provider The OAuth2 provider name (e.g., "google", "github"). Must not be null.
     * @param providerId The unique user ID from the OAuth2 provider. Must not be null.
     * @return An {@link Optional} containing the found user, or an empty
     *         {@link Optional} if no user exists for that provider and ID combination.
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}

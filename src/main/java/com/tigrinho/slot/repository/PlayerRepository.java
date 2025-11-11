package com.tigrinho.slot.repository;

import com.tigrinho.slot.model.entity.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link Player} entities.
 * This interface extends {@link MongoRepository} to provide standard CRUD operations
 * and custom query methods for player data in MongoDB.
 */
@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {

    /**
     * Finds a player by their unique username.
     * Spring Data automatically implements this method based on the method name.
     *
     * @param username The username to search for.
     * @return An {@link Optional} containing the {@link Player} if found, or empty otherwise.
     */
    Optional<Player> findByUsername(final String username);
}

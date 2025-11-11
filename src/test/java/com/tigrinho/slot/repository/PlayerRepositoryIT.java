package com.tigrinho.slot.repository;

import com.tigrinho.slot.TestContainersConfiguration;
import com.tigrinho.slot.model.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for the {@link PlayerRepository}.
 * This class uses {@link DataMongoTest} to load only the persistence context,
 * making tests faster. It imports {@link TestContainersConfiguration} to
 * provide a real MongoDB instance via Testcontainers.
 */
@DataMongoTest
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
public class PlayerRepositoryIT {

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * Cleans the database before EACH test to ensure test isolation.
     */
    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();
    }

    /**
     * Tests that a player can be successfully saved and retrieved from the database.
     */
    @Test
    @DisplayName("Should save and retrieve a player successfully")
    void shouldSaveAndRetrievePlayerSuccessfully() {
        // Given
        final Player newPlayer = Player.builder()
                .username("owl_user")
                .hashedPassword("password123")
                .balance(new BigDecimal("100.00"))
                .build();

        // When
        final Player savedPlayer = playerRepository.save(newPlayer);

        // Then
        assertThat(savedPlayer).isNotNull();
        assertThat(savedPlayer.getId()).isNotNull();
        assertThat(savedPlayer.getUsername()).isEqualTo("owl_user");
    }

    /**
     * Tests finding a player by their username.
     */
    @Test
    @DisplayName("Should find a player by username")
    void shouldFindPlayerByUsername() {
        // Given
        final Player player = Player.builder()
                .username("user_to_find")
                .hashedPassword("pass")
                .balance(new BigDecimal("50.00"))
                .build();
        playerRepository.save(player);

        // When
        final Optional<Player> foundPlayer = playerRepository.findByUsername("user_to_find");

        // Then
        assertThat(foundPlayer).isPresent();
        assertThat(foundPlayer.get().getUsername()).isEqualTo("user_to_find");
    }

    /**
     * Tests that saving a player with a duplicate username throws a {@link DuplicateKeyException}.
     */
    @Test
    @DisplayName("Should fail when trying to save a duplicate username")
    void shouldFailOnDuplicateUsername() {
        // Given
        final Player player1 = Player.builder()
                .username("duplicate_user")
                .hashedPassword("pass1")
                .balance(new BigDecimal("10.00"))
                .build();
        playerRepository.save(player1);

        final Player player2 = Player.builder()
                .username("duplicate_user") // Same username
                .hashedPassword("pass2")
                .balance(new BigDecimal("20.00"))
                .build();

        // When & Then
        assertThatThrownBy(() -> playerRepository.save(player2))
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining("duplicate key error collection");
    }

    /**
     * Tests that searching for a non-existent username returns an empty Optional.
     */
    @Test
    @DisplayName("Should not find a non-existent player by username")
    void shouldNotFindNonExistentPlayer() {
        // When
        final Optional<Player> foundPlayer = playerRepository.findByUsername("ghost_user");

        // Then
        assertThat(foundPlayer).isNotPresent();
    }

    /**
     * Tests that saving an existing player updates their data instead of creating a new one.
     */
    @Test
    @DisplayName("Should update an existing player instead of creating a new one")
    void shouldUpdateExistingPlayer() {
        // Given
        // 1. Create and save a player with an initial balance
        Player originalPlayer = playerRepository.save(Player.builder()
                .username("player_to_update")
                .hashedPassword("old_password")
                .balance(new BigDecimal("100.00"))
                .build());
        assertThat(originalPlayer.getId()).isNotNull();

        // 2. Modify the in-memory object
        originalPlayer.setBalance(new BigDecimal("500.00"));
        originalPlayer.setHashedPassword("new_password");

        // When
        // 3. Save the modified object
        playerRepository.save(originalPlayer);

        // Then
        // 4. Fetch the player by ID to verify changes
        final Optional<Player> updatedPlayerOpt = playerRepository.findById(originalPlayer.getId());
        assertThat(updatedPlayerOpt).isPresent();
        final Player updatedPlayer = updatedPlayerOpt.get();

        // 5. Verify that fields were updated
        assertThat(updatedPlayer.getBalance()).isEqualByComparingTo("500.00");
        assertThat(updatedPlayer.getHashedPassword()).isEqualTo("new_password");

        // 6. Ensure no new player was created
        assertThat(playerRepository.count()).isEqualTo(1);
    }
}

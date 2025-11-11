package com.tigrinho.slot.service;

import com.tigrinho.slot.exception.ResourceNotFoundException;
import com.tigrinho.slot.exception.UsernameAlreadyExistsException;
import com.tigrinho.slot.mapper.PlayerMapper;
import com.tigrinho.slot.model.dto.request.CreatePlayerRequest;
import com.tigrinho.slot.model.dto.request.UpdateSeedsRequest;
import com.tigrinho.slot.model.dto.response.ChangeSeedsResponse;
import com.tigrinho.slot.model.dto.response.PlayerResponse;
import com.tigrinho.slot.model.dto.response.ProvablyFairResponse;
import com.tigrinho.slot.model.entity.Player;
import com.tigrinho.slot.repository.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link PlayerService}.
 * This class uses Mockito to isolate the service logic and verify interactions
 * with its dependencies ({@link PlayerRepository}, {@link PlayerMapper},
 * {@link PasswordEncoder}, {@link CryptoService}).
 */
@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PlayerMapper playerMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CryptoService cryptoService;

    @InjectMocks
    private PlayerService playerService;

    // --- Tests for createPlayer ---

    /**
     * Tests successful player creation, including the generation and assignment
     * of Provably Fair seeds and initial balance.
     */
    @Test
    @DisplayName("Should create a player successfully, including Provably Fair data")
    void createPlayer_Success() {
        // Given
        final CreatePlayerRequest request = new CreatePlayerRequest("newUser", "password123");
        final Player mappedPlayer = new Player();
        final Player savedPlayer = new Player();
        final PlayerResponse responseDto = new PlayerResponse("player123", "newUser", new BigDecimal("100.00"));

        when(playerRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(playerMapper.toPlayer(request)).thenReturn(mappedPlayer);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(cryptoService.generateSeed()).thenReturn("new-server-seed");
        when(cryptoService.hash("new-server-seed")).thenReturn("hash-of-new-server-seed");
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);
        when(playerMapper.toPlayerResponse(savedPlayer)).thenReturn(responseDto);

        // When
        final PlayerResponse response = playerService.createPlayer(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("newUser");

        // Capture the Player object passed to the save method
        final ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        final Player playerToSave = playerCaptor.getValue();

        // Verify all fields were set correctly before saving
        assertThat(playerToSave.getBalance()).isEqualByComparingTo("100.00");
        assertThat(playerToSave.getHashedPassword()).isEqualTo("hashed_password");
        assertThat(playerToSave.getServerSeed()).isEqualTo("new-server-seed");
        assertThat(playerToSave.getServerSeedHash()).isEqualTo("hash-of-new-server-seed");
        assertThat(playerToSave.getClientSeed()).isEqualTo("default-client-seed");
        assertThat(playerToSave.getNonce()).isEqualTo(0L);
    }

    /**
     * Tests that an {@link UsernameAlreadyExistsException} is thrown when
     * attempting to create a player with an existing username.
     */
    @Test
    @DisplayName("Should throw exception if username already exists when creating player")
    void createPlayer_UsernameExists() {
        // Given
        final CreatePlayerRequest request = new CreatePlayerRequest("existingUser", "password123");
        when(playerRepository.findByUsername("existingUser")).thenReturn(Optional.of(new Player()));

        // When & Then
        assertThatThrownBy(() -> playerService.createPlayer(request))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(playerRepository, never()).save(any());
        verify(cryptoService, never()).generateSeed();
    }

    // --- Tests for getProvablyFairData ---

    /**
     * Tests successful retrieval of Provably Fair data for an existing player.
     */
    @Test
    @DisplayName("Should return Provably Fair data for an existing player")
    void getProvablyFairData_Success() {
        // Given
        final String playerId = "player1";
        final Player player = Player.builder()
                .serverSeedHash("existing-hash")
                .clientSeed("existing-client-seed")
                .nonce(10L)
                .build();
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // When
        final ProvablyFairResponse response = playerService.getProvablyFairData(playerId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.serverSeedHash()).isEqualTo("existing-hash");
        assertThat(response.clientSeed()).isEqualTo("existing-client-seed");
        assertThat(response.nonce()).isEqualTo(10L);
    }

    /**
     * Tests that a {@link ResourceNotFoundException} is thrown when
     * attempting to retrieve Provably Fair data for a non-existent player.
     */
    @Test
    @DisplayName("Should throw exception when fetching PF data for non-existent player")
    void getProvablyFairData_PlayerNotFound() {
        // Given
        final String playerId = "ghost-player";
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> playerService.getProvablyFairData(playerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- Tests for changeSeeds ---

    /**
     * Tests successful changing of player seeds.
     * Verifies that new seeds are generated, nonce is reset, and the player is saved.
     */
    @Test
    @DisplayName("Should change player seeds successfully")
    void changeSeeds_Success() {
        // Given
        final String playerId = "player1";
        final Player player = Player.builder()
                .id(playerId)
                .serverSeed("old-server-seed")
                .nonce(10L)
                .build();
        final UpdateSeedsRequest request = new UpdateSeedsRequest("new-client-seed");

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(cryptoService.generateSeed()).thenReturn("new-server-seed-generated");
        when(cryptoService.hash("new-server-seed-generated")).thenReturn("hash-of-new-generated-seed");

        // When
        final ChangeSeedsResponse response = playerService.changeSeeds(playerId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.oldServerSeed()).isEqualTo("old-server-seed");
        assertThat(response.newClientSeed()).isEqualTo("new-client-seed");
        assertThat(response.newServerSeedHash()).isEqualTo("hash-of-new-generated-seed");
        assertThat(response.newNonce()).isEqualTo(0L);

        // Capture the player to verify it was updated correctly
        final ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        final Player updatedPlayer = playerCaptor.getValue();

        assertThat(updatedPlayer.getNonce()).isEqualTo(0L); // Verify nonce was reset
        assertThat(updatedPlayer.getClientSeed()).isEqualTo("new-client-seed");
        assertThat(updatedPlayer.getServerSeed()).isEqualTo("new-server-seed-generated");
        assertThat(updatedPlayer.getServerSeedHash()).isEqualTo("hash-of-new-generated-seed");
    }

    /**
     * Tests that a {@link ResourceNotFoundException} is thrown when
     * attempting to change seeds for a non-existent player.
     */
    @Test
    @DisplayName("Should throw exception when changing seeds for non-existent player")
    void changeSeeds_PlayerNotFound() {
        // Given
        final String playerId = "ghost-player";
        final UpdateSeedsRequest request = new UpdateSeedsRequest("any-seed");
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> playerService.changeSeeds(playerId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(playerRepository, never()).save(any());
    }
}

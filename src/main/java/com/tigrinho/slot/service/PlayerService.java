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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service responsible for managing player-related business logic,
 * including player creation, Provably Fair data retrieval, and seed management.
 */
@Service
@RequiredArgsConstructor // Injects dependencies via constructor (Lombok)
public class PlayerService {

    // Initial balance for new players
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100.00");

    // Default client seed for new players (they can change this later)
    private static final String DEFAULT_CLIENT_SEED = "default-client-seed";

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final PasswordEncoder passwordEncoder;
    private final CryptoService cryptoService;

    /**
     * Retrieves Provably Fair data for a specific player.
     *
     * @param playerId The unique identifier of the player.
     * @return A {@link ProvablyFairResponse} containing the player's server seed hash, client seed, and nonce.
     * @throws ResourceNotFoundException if the player is not found.
     */
    public ProvablyFairResponse getProvablyFairData(final String playerId) {
        final Player player = findPlayerById(playerId);

        return new ProvablyFairResponse(
                player.getServerSeedHash(),
                player.getClientSeed(),
                player.getNonce()
        );
    }

    /**
     * Changes the client and server seeds for a player's Provably Fair system.
     * This operation generates a new server seed, hashes it, updates the client seed,
     * and resets the nonce to 0.
     *
     * @param playerId The unique identifier of the player.
     * @param request The {@link UpdateSeedsRequest} containing the new client seed.
     * @return A {@link ChangeSeedsResponse} with the old server seed (for verification),
     *         new server seed hash, new client seed, and the reset nonce.
     * @throws ResourceNotFoundException if the player is not found.
     */
    public ChangeSeedsResponse changeSeeds(final String playerId, final UpdateSeedsRequest request) {
        final Player player = findPlayerById(playerId);

        // 1. Get old data to return for verification
        final String oldServerSeed = player.getServerSeed();

        // 2. Generate new server seeds
        final String newServerSeed = cryptoService.generateSeed();
        final String newServerSeedHash = cryptoService.hash(newServerSeed);
        final String newClientSeed = request.clientSeed();

        // 3. Update the player with new data
        player.setServerSeed(newServerSeed);
        player.setServerSeedHash(newServerSeedHash);
        player.setClientSeed(newClientSeed);
        player.setNonce(0L); // Reset spin counter

        // 4. Save to database
        playerRepository.save(player);

        // 5. Return the response with the OLD server seed (for verification)
        return new ChangeSeedsResponse(
                oldServerSeed,      // The proof!
                newServerSeedHash,  // The new hash
                newClientSeed,      // The new client seed
                0L                  // The new nonce
        );
    }

    /**
     * Helper method to find a player by their ID or throw a {@link ResourceNotFoundException}.
     *
     * @param playerId The unique identifier of the player.
     * @return The {@link Player} entity if found.
     * @throws ResourceNotFoundException if the player is not found.
     */
    private Player findPlayerById(final String playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", playerId));
    }

    /**
     * Creates a new player in the system.
     * This involves validating the username, encrypting the password,
     * setting an initial balance, and generating initial Provably Fair seeds.
     *
     * @param request The {@link CreatePlayerRequest} containing the new player's details.
     * @return A {@link PlayerResponse} for the newly created player.
     * @throws UsernameAlreadyExistsException if a player with the given username already exists.
     */
    public PlayerResponse createPlayer(final CreatePlayerRequest request) {
        // 1. Validate if the username already exists
        playerRepository.findByUsername(request.username())
                .ifPresent(player -> {
                    throw new UsernameAlreadyExistsException(request.username());
                });

        // 2. Map DTO to Entity
        final Player player = playerMapper.toPlayer(request);

        // 3. Encrypt the password
        player.setHashedPassword(passwordEncoder.encode(request.password()));

        // 4. Set the initial balance
        player.setBalance(INITIAL_BALANCE);

        // 5. "Arm" the player with Initial Provably Fair Seeds
        final String serverSeed = cryptoService.generateSeed();
        final String serverSeedHash = cryptoService.hash(serverSeed);

        player.setServerSeed(serverSeed);           // Saves the secret
        player.setServerSeedHash(serverSeedHash);   // Saves the proof (hash)
        player.setClientSeed(DEFAULT_CLIENT_SEED);  // Saves the client's secret
        player.setNonce(0L);                        // Resets the spin counter

        // 6. Save to database
        final Player savedPlayer = playerRepository.save(player);

        // 7. Map to response DTO and return
        return playerMapper.toPlayerResponse(savedPlayer);
    }
}

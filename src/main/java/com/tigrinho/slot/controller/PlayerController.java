package com.tigrinho.slot.controller;

import com.tigrinho.slot.model.dto.request.CreatePlayerRequest;
import com.tigrinho.slot.model.dto.request.SpinRequest;
import com.tigrinho.slot.model.dto.request.UpdateSeedsRequest;
import com.tigrinho.slot.model.dto.response.ChangeSeedsResponse;
import com.tigrinho.slot.model.dto.response.PlayerResponse;
import com.tigrinho.slot.model.dto.response.ProvablyFairResponse;
import com.tigrinho.slot.model.dto.response.SpinResponse;
import com.tigrinho.slot.service.GameService;
import com.tigrinho.slot.service.PlayerService;
import com.tigrinho.slot.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;

/**
 * REST controller for managing player-related operations, including creation,
 * wallet balance, game spins, and Provably Fair seed management.
 */
@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
@Tag(name = "Player", description = "Endpoints for player management and wallet")
public class PlayerController {

    private final PlayerService playerService;
    private final WalletService walletService;
    private final GameService gameService;

    /**
     * Creates a new player in the system.
     *
     * @param request The {@link CreatePlayerRequest} containing player details.
     * @return A {@link ResponseEntity} with the created {@link PlayerResponse} and HTTP status 201.
     */
    @Operation(summary = "Create a new player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Player created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (validation error)"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody final CreatePlayerRequest request) {
        final PlayerResponse response = playerService.createPlayer(request);

        // Returns 201 Created with the URL of the new resource in the "Location" header
        final URI location = URI.create(String.format("/api/v1/players/%s", response.id()));
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Retrieves the wallet balance for a specific player.
     *
     * @param playerId The unique identifier of the player.
     * @return A {@link ResponseEntity} with the player's {@link BigDecimal} balance and HTTP status 200.
     */
    @Operation(summary = "Get player's wallet balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @GetMapping("/{playerId}/wallet/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable final String playerId) {
        final BigDecimal balance = walletService.getBalance(playerId);
        return ResponseEntity.ok(balance);
    }

    /**
     * Performs a game spin for a specific player with a given bet amount.
     *
     * @param playerId The unique identifier of the player.
     * @param request The {@link SpinRequest} containing the bet amount.
     * @return A {@link ResponseEntity} with the {@link SpinResponse} and HTTP status 200.
     */
    @Operation(summary = "Perform a game spin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Spin successful"),
            @ApiResponse(responseCode = "400", description = "Invalid bet amount"),
            @ApiResponse(responseCode = "402", description = "Insufficient funds")
    })
    @PostMapping("/{playerId}/spin")
    public ResponseEntity<SpinResponse> performSpin(
            @PathVariable final String playerId,
            @Valid @RequestBody final SpinRequest request) {

        final SpinResponse response = gameService.performSpin(
                playerId,
                request.betAmount()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves Provably Fair data for a specific player.
     * This includes client seed, server seed hash, and nonce.
     *
     * @param playerId The unique identifier of the player.
     * @return A {@link ResponseEntity} with the {@link ProvablyFairResponse} and HTTP status 200.
     */
    @Operation(summary = "Get player's Provably Fair data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @GetMapping("/{playerId}/provably-fair")
    public ResponseEntity<ProvablyFairResponse> getProvablyFairData(@PathVariable final String playerId) {
        final ProvablyFairResponse response = playerService.getProvablyFairData(playerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Changes the client and server seeds for a player's Provably Fair system.
     * This action typically resets the nonce to 0.
     *
     * @param playerId The unique identifier of the player.
     * @param request The {@link UpdateSeedsRequest} containing the new client seed.
     * @return A {@link ResponseEntity} with the {@link ChangeSeedsResponse} and HTTP status 200.
     */
    @Operation(summary = "Change player's seeds (clientSeed and serverSeed)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seeds changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid client seed"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    @PostMapping("/{playerId}/provably-fair/seeds")
    public ResponseEntity<ChangeSeedsResponse> changeSeeds(
            @PathVariable final String playerId,
            @Valid @RequestBody final UpdateSeedsRequest request) {

        final ChangeSeedsResponse response = playerService.changeSeeds(playerId, request);
        return ResponseEntity.ok(response);
    }
}

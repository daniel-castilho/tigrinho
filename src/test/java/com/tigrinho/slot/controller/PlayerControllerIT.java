package com.tigrinho.slot.controller;

import com.tigrinho.slot.TestContainersConfiguration;
import com.tigrinho.slot.model.dto.request.CreatePlayerRequest;
import com.tigrinho.slot.model.dto.request.SpinRequest;
import com.tigrinho.slot.model.dto.request.UpdateSeedsRequest;
import com.tigrinho.slot.model.dto.response.PlayerResponse;
import com.tigrinho.slot.repository.PlayerRepository;
import com.tigrinho.slot.service.PlayerService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for the {@link PlayerController}.
 * This class uses {@link SpringBootTest} with a random port and {@link TestContainersConfiguration}
 * to provide a full application context and real infrastructure services (MongoDB, Redis, RabbitMQ).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
public class PlayerControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerService playerService; // Injected for pre-populating data

    /**
     * Sets up the test environment before each test.
     * Configures RestAssured base URI and port, and clears the database.
     */
    @BeforeEach
    void setUp() {
        // Configure RestAssured
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        // Clear the database before EACH test to ensure isolation
        playerRepository.deleteAll();
    }

    /**
     * Tests successful player creation.
     * Verifies HTTP status 201, Location header, and response body content.
     */
    @Test
    @DisplayName("Should create a player and return 201 Created")
    void createPlayer_Success() {
        // Given
        final CreatePlayerRequest request = new CreatePlayerRequest("api_user_success", "password123");

        // When & Then
        final PlayerResponse response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/players")
                .then()
                .statusCode(HttpStatus.CREATED.value()) // 201
                .header("Location", notNullValue()) // Verify Location header is set
                .extract()
                .as(PlayerResponse.class);

        // Assertions
        assertThat(response.id()).isNotNull();
        assertThat(response.username()).isEqualTo("api_user_success");
        assertThat(response.balance()).isEqualByComparingTo("100.00");
    }

    /**
     * Tests creating a player with a duplicate username, expecting a 409 Conflict.
     */
    @Test
    @DisplayName("Should return 409 Conflict when trying to create player with duplicate username")
    void createPlayer_UsernameConflict() {
        // Given
        playerService.createPlayer(new CreatePlayerRequest("existing_user", "password123"));
        final CreatePlayerRequest duplicateRequest = new CreatePlayerRequest("existing_user", "another_password");

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(duplicateRequest)
                .when()
                .post("/api/v1/players")
                .then()
                .statusCode(HttpStatus.CONFLICT.value()); // Expect 409
    }

    /**
     * Tests creating a player with invalid input data, expecting a 400 Bad Request.
     */
    @Test
    @DisplayName("Should return 400 Bad Request for invalid creation data")
    void createPlayer_InvalidInput() {
        // Given
        final CreatePlayerRequest invalidRequest = new CreatePlayerRequest("", "password123"); // Blank username

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/v1/players")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value()); // Expect 400
    }

    /**
     * Tests retrieving the balance of an existing player, expecting a 200 OK.
     */
    @Test
    @DisplayName("Should return balance for an existing player")
    void getBalance_Success() {
        // Given
        final PlayerResponse createdPlayer = playerService.createPlayer(new CreatePlayerRequest("balance_user", "password123"));
        final String playerId = createdPlayer.id();

        // When
        final String responseBody = given()
                .when()
                .get("/api/v1/players/{playerId}/wallet/balance", playerId)
                .then()
                .statusCode(HttpStatus.OK.value()) // Expect 200
                .extract().body().asString();

        // Then
        final BigDecimal balance = new BigDecimal(responseBody);
        assertThat(balance).isEqualByComparingTo("100.00");
    }

    /**
     * Tests retrieving the balance for a non-existent player, expecting a 404 Not Found.
     */
    @Test
    @DisplayName("Should return 404 Not Found when fetching balance for non-existent player")
    void getBalance_PlayerNotFound() {
        // Given
        final String nonExistentPlayerId = "non-existent-player";

        // When & Then
        given()
                .when()
                .get("/api/v1/players/{playerId}/wallet/balance", nonExistentPlayerId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value()); // Expect 404
    }

    /**
     * Tests performing a game spin successfully, expecting a 200 OK.
     */
    @Test
    @DisplayName("Should perform a spin successfully and return 200 OK")
    void performSpin_Success() {
        // Given
        final PlayerResponse createdPlayer = playerService.createPlayer(new CreatePlayerRequest("spin_user", "password123"));
        final String playerId = createdPlayer.id();
        final SpinRequest spinRequest = new SpinRequest(new BigDecimal("10.00"));

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(spinRequest)
                .when()
                .post("/api/v1/players/{playerId}/spin", playerId)
                .then()
                .statusCode(HttpStatus.OK.value()) // Expect 200
                .body("newBalance", notNullValue())
                .body("winAmount", notNullValue())
                .body("symbols", notNullValue());
    }

    /**
     * Tests performing a spin with insufficient funds, expecting a 402 Payment Required.
     */
    @Test
    @DisplayName("Should return 402 Payment Required when trying to spin with insufficient funds")
    void performSpin_InsufficientFunds() {
        // Given
        final PlayerResponse createdPlayer = playerService.createPlayer(new CreatePlayerRequest("low_balance_user", "password123"));
        final String playerId = createdPlayer.id();
        final SpinRequest spinRequest = new SpinRequest(new BigDecimal("200.00")); // Bet higher than initial balance

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(spinRequest)
                .when()
                .post("/api/v1/players/{playerId}/spin", playerId)
                .then()
                .statusCode(HttpStatus.PAYMENT_REQUIRED.value()); // Expect 402
    }

    /**
     * Tests performing a spin with an invalid bet amount, expecting a 400 Bad Request.
     */
    @Test
    @DisplayName("Should return 400 Bad Request when trying to spin with invalid bet amount")
    void performSpin_InvalidBet() {
        // Given
        final PlayerResponse createdPlayer = playerService.createPlayer(new CreatePlayerRequest("invalid_bet_user", "password123"));
        final String playerId = createdPlayer.id();
        final SpinRequest spinRequest = new SpinRequest(BigDecimal.ZERO); // Zero bet amount

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(spinRequest)
                .when()
                .post("/api/v1/players/{playerId}/spin", playerId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value()); // Expect 400
    }

    /**
     * Tests retrieving Provably Fair data for an existing player, expecting a 200 OK.
     */
    @Test
    @DisplayName("Should return Provably Fair data for an existing player")
    void getProvablyFairData_Success() {
        // Given
        final PlayerResponse createdPlayer = playerService.createPlayer(new CreatePlayerRequest("pf_user", "password123"));
        final String playerId = createdPlayer.id();

        // When & Then
        given()
                .when()
                .get("/api/v1/players/{playerId}/provably-fair", playerId)
                .then()
                .statusCode(HttpStatus.OK.value()) // Expect 200
                .body("clientSeed", notNullValue())
                .body("serverSeedHash", notNullValue())
                .body("nonce", equalTo(0));
    }

    /**
     * Tests retrieving Provably Fair data for a non-existent player, expecting a 404 Not Found.
     */
    @Test
    @DisplayName("Should return 404 Not Found when fetching Provably Fair data for non-existent player")
    void getProvablyFairData_PlayerNotFound() {
        // Given
        final String nonExistentPlayerId = "non-existent-pf-player";

        // When & Then
        given()
                .when()
                .get("/api/v1/players/{playerId}/provably-fair", nonExistentPlayerId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value()); // Expect 404
    }

    /**
     * Tests successfully changing a player's seeds, expecting a 200 OK.
     */
    @Test
    @DisplayName("Should change seeds successfully")
    void changeSeeds_Success() {
        // Given
        final PlayerResponse createdPlayer = playerService.createPlayer(new CreatePlayerRequest("seed_change_user", "password123"));
        final String playerId = createdPlayer.id();
        final UpdateSeedsRequest request = new UpdateSeedsRequest("new-client-seed");

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/players/{playerId}/provably-fair/seeds", playerId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("newClientSeed", equalTo("new-client-seed"))
                .body("newServerSeedHash", notNullValue())
                .body("newNonce", equalTo(0)); // Corrected field name
    }

    /**
     * Tests changing seeds with an invalid client seed, expecting a 400 Bad Request.
     */
    @Test
    @DisplayName("Should return 400 Bad Request when trying to change seeds with invalid clientSeed")
    void changeSeeds_InvalidClientSeed() {
        // Given
        final PlayerResponse createdPlayer = playerService.createPlayer(new CreatePlayerRequest("invalid_seed_user", "password123"));
        final String playerId = createdPlayer.id();
        final UpdateSeedsRequest request = new UpdateSeedsRequest(""); // Blank clientSeed

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/players/{playerId}/provably-fair/seeds", playerId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Tests changing seeds for a non-existent player, expecting a 404 Not Found.
     */
    @Test
    @DisplayName("Should return 404 Not Found when trying to change seeds for non-existent player")
    void changeSeeds_PlayerNotFound() {
        // Given
        final String nonExistentPlayerId = "non-existent-seed-player";
        final UpdateSeedsRequest request = new UpdateSeedsRequest("any-seed");

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/players/{playerId}/provably-fair/seeds", nonExistentPlayerId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}

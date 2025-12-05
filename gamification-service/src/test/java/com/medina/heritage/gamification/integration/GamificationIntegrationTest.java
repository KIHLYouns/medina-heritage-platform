package com.medina.heritage.gamification.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medina.heritage.gamification.dto.request.AddPointsRequest;
import com.medina.heritage.gamification.dto.request.DeductPointsRequest;
import com.medina.heritage.gamification.entity.Wallet;
import com.medina.heritage.gamification.repository.PointTransactionRepository;
import com.medina.heritage.gamification.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Gamification Service.
 * Uses H2 in-memory database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GamificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PointTransactionRepository transactionRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        testUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    }

    private Wallet createAndSaveWallet(UUID userId, int balance) {
        Wallet wallet = new Wallet(userId);
        wallet.setBalance(balance);
        wallet.setTotalEarned(balance);
        return walletRepository.save(wallet);
    }

    @Nested
    @DisplayName("Wallet Integration Tests")
    class WalletIntegrationTests {

        @Test
        @DisplayName("Should get or create wallet for new user")
        void shouldGetOrCreateWalletForNewUser() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/wallets/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(testUserId.toString()))
                    .andExpect(jsonPath("$.data.balance").value(0))
                    .andExpect(jsonPath("$.data.level").value(1));

            // Verify wallet was created
            assertThat(walletRepository.existsByUserId(testUserId)).isTrue();
        }

        @Test
        @DisplayName("Should return existing wallet")
        void shouldReturnExistingWallet() throws Exception {
            // Given
            createAndSaveWallet(testUserId, 500);

            // When/Then
            mockMvc.perform(get("/api/wallets/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.balance").value(500));
        }

        @Test
        @DisplayName("Should return balance for user")
        void shouldReturnBalanceForUser() throws Exception {
            // Given
            createAndSaveWallet(testUserId, 250);

            // When/Then
            mockMvc.perform(get("/api/wallets/{userId}/balance", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(250));
        }

        @Test
        @DisplayName("Should return level for user")
        void shouldReturnLevelForUser() throws Exception {
            // Given
            Wallet wallet = new Wallet(testUserId);
            wallet.setBalance(1500);
            wallet.setTotalEarned(1500);
            wallet.setLevel(2); // Level 2 for 1500 points
            walletRepository.save(wallet);

            // When/Then
            mockMvc.perform(get("/api/wallets/{userId}/level", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(2));
        }
    }

    @Nested
    @DisplayName("Add Points Integration Tests")
    class AddPointsIntegrationTests {

        @Test
        @DisplayName("Should add points to new user and create wallet")
        void shouldAddPointsToNewUserAndCreateWallet() throws Exception {
            // Given
            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(100);
            request.setReasonCode("REPORT_VALIDATED");
            request.setDescription("Test report validated");

            // When/Then
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.points").value(100))
                    .andExpect(jsonPath("$.data.transactionType").value("CREDIT"))
                    .andExpect(jsonPath("$.data.reasonCode").value("REPORT_VALIDATED"))
                    .andExpect(jsonPath("$.data.balanceAfter").value(100));

            // Verify database state
            Wallet wallet = walletRepository.findByUserId(testUserId).orElseThrow();
            assertThat(wallet.getBalance()).isEqualTo(100);
            assertThat(wallet.getTotalEarned()).isEqualTo(100);

            // Verify transaction was created
            assertThat(transactionRepository.findByUserIdOrderByCreatedAtDesc(testUserId)).hasSize(1);
        }

        @Test
        @DisplayName("Should add points to existing wallet")
        void shouldAddPointsToExistingWallet() throws Exception {
            // Given
            createAndSaveWallet(testUserId, 100);

            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50);
            request.setReasonCode("FIRST_REPORT");

            // When/Then
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.balanceAfter").value(150));

            // Verify database state
            Wallet wallet = walletRepository.findByUserId(testUserId).orElseThrow();
            assertThat(wallet.getBalance()).isEqualTo(150);
        }

        @Test
        @DisplayName("Should prevent duplicate transactions")
        void shouldPreventDuplicateTransactions() throws Exception {
            // Given
            UUID reportId = UUID.randomUUID();
            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(100);
            request.setReasonCode("REPORT_VALIDATED");
            request.setReferenceId(reportId);
            request.setReferenceType("REPORT");

            // First request should succeed
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Second request with same reference should fail
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should update level when reaching threshold")
        void shouldUpdateLevelWhenReachingThreshold() throws Exception {
            // Given - user with 950 points (needs 50 more for level 2)
            Wallet wallet = new Wallet(testUserId);
            wallet.setBalance(950);
            wallet.setTotalEarned(950);
            wallet.setLevel(1);
            walletRepository.save(wallet);

            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(100);
            request.setReasonCode("REPORT_VALIDATED");

            // When/Then
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Verify level was updated
            Wallet updatedWallet = walletRepository.findByUserId(testUserId).orElseThrow();
            assertThat(updatedWallet.getLevel()).isEqualTo(2); // 1050 points = level 2
        }
    }

    @Nested
    @DisplayName("Deduct Points Integration Tests")
    class DeductPointsIntegrationTests {

        @Test
        @DisplayName("Should deduct points successfully")
        void shouldDeductPointsSuccessfully() throws Exception {
            // Given
            createAndSaveWallet(testUserId, 200);

            DeductPointsRequest request = new DeductPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50);
            request.setReasonCode("REWARD_REDEMPTION");
            request.setDescription("Redeemed voucher");

            // When/Then
            mockMvc.perform(post("/api/wallets/deduct-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.points").value(50))
                    .andExpect(jsonPath("$.data.transactionType").value("DEBIT"))
                    .andExpect(jsonPath("$.data.balanceAfter").value(150));

            // Verify database state
            Wallet wallet = walletRepository.findByUserId(testUserId).orElseThrow();
            assertThat(wallet.getBalance()).isEqualTo(150);
            assertThat(wallet.getTotalSpent()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should return 404 for non-existing wallet")
        void shouldReturn404ForNonExistingWallet() throws Exception {
            // Given
            DeductPointsRequest request = new DeductPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50);
            request.setReasonCode("REWARD_REDEMPTION");

            // When/Then
            mockMvc.perform(post("/api/wallets/deduct-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for insufficient balance")
        void shouldReturn400ForInsufficientBalance() throws Exception {
            // Given
            createAndSaveWallet(testUserId, 30);

            DeductPointsRequest request = new DeductPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50); // More than available balance
            request.setReasonCode("REWARD_REDEMPTION");

            // When/Then
            mockMvc.perform(post("/api/wallets/deduct-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Transaction History Integration Tests")
    class TransactionHistoryIntegrationTests {

        @Test
        @DisplayName("Should return transaction history for user")
        void shouldReturnTransactionHistoryForUser() throws Exception {
            // Given - Create wallet and add some transactions
            AddPointsRequest addRequest = new AddPointsRequest();
            addRequest.setUserId(testUserId);
            addRequest.setPoints(100);
            addRequest.setReasonCode("REPORT_VALIDATED");

            // Add points twice
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isCreated());

            addRequest.setPoints(50);
            addRequest.setReasonCode("FIRST_REPORT");
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isCreated());

            // When/Then
            mockMvc.perform(get("/api/transactions/user/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("Should return recent transactions for user")
        void shouldReturnRecentTransactionsForUser() throws Exception {
            // Given
            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(100);
            request.setReasonCode("REPORT_VALIDATED");

            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // When/Then
            mockMvc.perform(get("/api/transactions/user/{userId}/recent", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].points").value(100));
        }

        @Test
        @DisplayName("Should return total points earned")
        void shouldReturnTotalPointsEarned() throws Exception {
            // Given
            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(100);
            request.setReasonCode("REPORT_VALIDATED");

            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            request.setPoints(50);
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // When/Then
            mockMvc.perform(get("/api/transactions/user/{userId}/total-earned", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(150));
        }
    }

    @Nested
    @DisplayName("Validation Integration Tests")
    class ValidationIntegrationTests {

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() throws Exception {
            // Given
            AddPointsRequest request = new AddPointsRequest();
            // Missing all required fields

            // When/Then
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 for invalid points value")
        void shouldReturn400ForInvalidPointsValue() throws Exception {
            // Given
            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(0); // Must be at least 1
            request.setReasonCode("REPORT_VALIDATED");

            // When/Then
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}

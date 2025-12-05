package com.medina.heritage.gamification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medina.heritage.gamification.dto.request.AddPointsRequest;
import com.medina.heritage.gamification.dto.request.DeductPointsRequest;
import com.medina.heritage.gamification.dto.response.PointTransactionResponse;
import com.medina.heritage.gamification.dto.response.WalletResponse;
import com.medina.heritage.gamification.enums.TransactionType;
import com.medina.heritage.gamification.exception.GlobalExceptionHandler;
import com.medina.heritage.gamification.exception.InsufficientBalanceException;
import com.medina.heritage.gamification.exception.WalletNotFoundException;
import com.medina.heritage.gamification.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    private UUID testUserId;
    private WalletResponse testWalletResponse;
    private PointTransactionResponse testTransactionResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(walletController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        testWalletResponse = new WalletResponse();
        testWalletResponse.setUserId(testUserId);
        testWalletResponse.setBalance(100);
        testWalletResponse.setLevel(1);
        testWalletResponse.setTotalEarned(100);
        testWalletResponse.setTotalSpent(0);
        testWalletResponse.setPointsToNextLevel(900);
        testWalletResponse.setProgressPercentage(10);
        testWalletResponse.setLastUpdatedAt(OffsetDateTime.now());

        testTransactionResponse = new PointTransactionResponse();
        testTransactionResponse.setId(UUID.randomUUID());
        testTransactionResponse.setUserId(testUserId);
        testTransactionResponse.setPoints(50);
        testTransactionResponse.setTransactionType(TransactionType.CREDIT);
        testTransactionResponse.setReasonCode("REPORT_VALIDATED");
        testTransactionResponse.setBalanceAfter(150);
        testTransactionResponse.setCreatedAt(OffsetDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/wallets/{userId}")
    class GetWalletTests {

        @Test
        @DisplayName("Should return wallet for user")
        void shouldReturnWalletForUser() throws Exception {
            // Given
            when(walletService.getOrCreateWallet(testUserId)).thenReturn(testWalletResponse);

            // When/Then
            mockMvc.perform(get("/api/wallets/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(testUserId.toString()))
                    .andExpect(jsonPath("$.data.balance").value(100))
                    .andExpect(jsonPath("$.data.level").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/wallets/{userId}/rank")
    class GetRankTests {

        @Test
        @DisplayName("Should return rank for user")
        void shouldReturnRankForUser() throws Exception {
            // Given
            when(walletService.getUserRank(testUserId)).thenReturn(5L);

            // When/Then
            mockMvc.perform(get("/api/wallets/{userId}/rank", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(5));
        }

        @Test
        @DisplayName("Should return null rank for new user")
        void shouldReturnNullRankForNewUser() throws Exception {
            // Given
            when(walletService.getUserRank(testUserId)).thenReturn(null);

            // When/Then
            mockMvc.perform(get("/api/wallets/{userId}/rank", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/wallets/add-points")
    class AddPointsTests {

        @Test
        @DisplayName("Should add points successfully")
        void shouldAddPointsSuccessfully() throws Exception {
            // Given
            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50);
            request.setReasonCode("REPORT_VALIDATED");
            request.setDescription("Test description");

            when(walletService.addPoints(any(AddPointsRequest.class))).thenReturn(testTransactionResponse);

            // When/Then
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.points").value(50))
                    .andExpect(jsonPath("$.data.transactionType").value("CREDIT"));
        }

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
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for negative points")
        void shouldReturn400ForNegativePoints() throws Exception {
            // Given
            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(0); // Invalid: must be at least 1
            request.setReasonCode("REPORT_VALIDATED");

            // When/Then
            mockMvc.perform(post("/api/wallets/add-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/wallets/deduct-points")
    class DeductPointsTests {

        @Test
        @DisplayName("Should deduct points successfully")
        void shouldDeductPointsSuccessfully() throws Exception {
            // Given
            DeductPointsRequest request = new DeductPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50);
            request.setReasonCode("REWARD_REDEMPTION");

            PointTransactionResponse debitResponse = new PointTransactionResponse();
            debitResponse.setId(UUID.randomUUID());
            debitResponse.setPoints(50);
            debitResponse.setTransactionType(TransactionType.DEBIT);

            when(walletService.deductPoints(any(DeductPointsRequest.class))).thenReturn(debitResponse);

            // When/Then
            mockMvc.perform(post("/api/wallets/deduct-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.points").value(50))
                    .andExpect(jsonPath("$.data.transactionType").value("DEBIT"));
        }

        @Test
        @DisplayName("Should return 400 for insufficient balance")
        void shouldReturn400ForInsufficientBalance() throws Exception {
            // Given
            DeductPointsRequest request = new DeductPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(1000);
            request.setReasonCode("REWARD_REDEMPTION");

            when(walletService.deductPoints(any(DeductPointsRequest.class)))
                    .thenThrow(new InsufficientBalanceException(1000, 100));

            // When/Then
            mockMvc.perform(post("/api/wallets/deduct-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 404 when wallet not found")
        void shouldReturn404WhenWalletNotFound() throws Exception {
            // Given
            DeductPointsRequest request = new DeductPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50);
            request.setReasonCode("REWARD_REDEMPTION");

            when(walletService.deductPoints(any(DeductPointsRequest.class)))
                    .thenThrow(new WalletNotFoundException(testUserId));

            // When/Then
            mockMvc.perform(post("/api/wallets/deduct-points")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/wallets/{userId}/exists")
    class WalletExistsTests {

        @Test
        @DisplayName("Should return true when wallet exists")
        void shouldReturnTrueWhenWalletExists() throws Exception {
            // Given
            when(walletService.walletExists(testUserId)).thenReturn(true);

            // When/Then
            mockMvc.perform(get("/api/wallets/{userId}/exists", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("Should return false when wallet does not exist")
        void shouldReturnFalseWhenWalletDoesNotExist() throws Exception {
            // Given
            when(walletService.walletExists(testUserId)).thenReturn(false);

            // When/Then
            mockMvc.perform(get("/api/wallets/{userId}/exists", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(false));
        }
    }
}

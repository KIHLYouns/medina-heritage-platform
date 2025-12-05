package com.medina.heritage.gamification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medina.heritage.gamification.dto.response.PointTransactionResponse;
import com.medina.heritage.gamification.enums.TransactionType;
import com.medina.heritage.gamification.service.PointTransactionService;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PointTransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PointTransactionService transactionService;

    @InjectMocks
    private PointTransactionController transactionController;

    private ObjectMapper objectMapper;
    private UUID testUserId;
    private UUID testTransactionId;
    private PointTransactionResponse testTransactionResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        testUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        testTransactionId = UUID.randomUUID();

        testTransactionResponse = new PointTransactionResponse();
        testTransactionResponse.setId(testTransactionId);
        testTransactionResponse.setUserId(testUserId);
        testTransactionResponse.setPoints(100);
        testTransactionResponse.setTransactionType(TransactionType.CREDIT);
        testTransactionResponse.setReasonCode("REPORT_VALIDATED");
        testTransactionResponse.setBalanceAfter(100);
        testTransactionResponse.setCreatedAt(OffsetDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/transactions/user/{userId}")
    class GetTransactionsByUserTests {

        @Test
        @DisplayName("Should return all transactions for user")
        void shouldReturnAllTransactionsForUser() throws Exception {
            // Given
            List<PointTransactionResponse> transactions = Arrays.asList(testTransactionResponse);

            when(transactionService.getTransactionsByUser(testUserId))
                    .thenReturn(transactions);

            // When/Then
            mockMvc.perform(get("/api/transactions/user/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].points").value(100));
        }
    }

    @Nested
    @DisplayName("GET /api/transactions/user/{userId}/type/{type}")
    class GetTransactionsByTypeTests {

        @Test
        @DisplayName("Should return transactions by type")
        void shouldReturnTransactionsByType() throws Exception {
            // Given
            List<PointTransactionResponse> transactions = Arrays.asList(testTransactionResponse);

            when(transactionService.getTransactionsByUserAndType(testUserId, TransactionType.CREDIT))
                    .thenReturn(transactions);

            // When/Then
            mockMvc.perform(get("/api/transactions/user/{userId}/type/{type}", testUserId, "CREDIT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/transactions/user/{userId}/recent")
    class GetRecentTransactionsTests {

        @Test
        @DisplayName("Should return recent transactions")
        void shouldReturnRecentTransactions() throws Exception {
            // Given
            List<PointTransactionResponse> transactions = Arrays.asList(testTransactionResponse);

            when(transactionService.getRecentTransactions(testUserId)).thenReturn(transactions);

            // When/Then
            mockMvc.perform(get("/api/transactions/user/{userId}/recent", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/transactions/reference/{referenceType}/{referenceId}")
    class GetTransactionsByReferenceTests {

        @Test
        @DisplayName("Should return transactions by reference")
        void shouldReturnTransactionsByReference() throws Exception {
            // Given
            UUID referenceId = UUID.randomUUID();
            List<PointTransactionResponse> transactions = Arrays.asList(testTransactionResponse);

            when(transactionService.getTransactionsByReference(referenceId, "REPORT"))
                    .thenReturn(transactions);

            // When/Then
            mockMvc.perform(get("/api/transactions/reference/{referenceType}/{referenceId}", 
                            "REPORT", referenceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/transactions/{id}")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("Should return transaction by ID")
        void shouldReturnTransactionById() throws Exception {
            // Given
            when(transactionService.getTransactionById(testTransactionId))
                    .thenReturn(testTransactionResponse);

            // When/Then
            mockMvc.perform(get("/api/transactions/{id}", testTransactionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testTransactionId.toString()));
        }
    }

    @Nested
    @DisplayName("GET /api/transactions/user/{userId}/total-earned")
    class GetTotalPointsEarnedTests {

        @Test
        @DisplayName("Should return total points earned")
        void shouldReturnTotalPointsEarned() throws Exception {
            // Given
            when(transactionService.getTotalPointsEarned(testUserId)).thenReturn(500);

            // When/Then
            mockMvc.perform(get("/api/transactions/user/{userId}/total-earned", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(500));
        }
    }

    @Nested
    @DisplayName("GET /api/transactions/user/{userId}/total-spent")
    class GetTotalPointsSpentTests {

        @Test
        @DisplayName("Should return total points spent")
        void shouldReturnTotalPointsSpent() throws Exception {
            // Given
            when(transactionService.getTotalPointsSpent(testUserId)).thenReturn(200);

            // When/Then
            mockMvc.perform(get("/api/transactions/user/{userId}/total-spent", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(200));
        }
    }
}

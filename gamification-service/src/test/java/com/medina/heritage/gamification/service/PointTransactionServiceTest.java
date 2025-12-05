package com.medina.heritage.gamification.service;

import com.medina.heritage.gamification.dto.response.PointTransactionResponse;
import com.medina.heritage.gamification.entity.PointTransaction;
import com.medina.heritage.gamification.enums.TransactionType;
import com.medina.heritage.gamification.mapper.PointTransactionMapper;
import com.medina.heritage.gamification.repository.PointTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointTransactionServiceTest {

    @Mock
    private PointTransactionRepository transactionRepository;

    @Mock
    private PointTransactionMapper transactionMapper;

    @InjectMocks
    private PointTransactionService transactionService;

    private UUID testUserId;
    private UUID testTransactionId;
    private PointTransaction testTransaction;
    private PointTransactionResponse testTransactionResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testTransactionId = UUID.randomUUID();

        testTransaction = new PointTransaction();
        testTransaction.setId(testTransactionId);
        testTransaction.setUserId(testUserId);
        testTransaction.setPoints(100);
        testTransaction.setTransactionType(TransactionType.CREDIT);
        testTransaction.setReasonCode("REPORT_VALIDATED");
        testTransaction.setDescription("Test transaction");
        testTransaction.setBalanceAfter(100);
        testTransaction.setCreatedAt(OffsetDateTime.now());

        testTransactionResponse = new PointTransactionResponse();
        testTransactionResponse.setId(testTransactionId);
        testTransactionResponse.setUserId(testUserId);
        testTransactionResponse.setPoints(100);
        testTransactionResponse.setTransactionType(TransactionType.CREDIT);
        testTransactionResponse.setReasonCode("REPORT_VALIDATED");
        testTransactionResponse.setBalanceAfter(100);
    }

    @Nested
    @DisplayName("Get Transactions By User Tests")
    class GetTransactionsByUserTests {

        @Test
        @DisplayName("Should return all transactions for user")
        void shouldReturnAllTransactionsForUser() {
            // Given
            List<PointTransaction> transactions = Arrays.asList(testTransaction);
            List<PointTransactionResponse> expectedResponses = Arrays.asList(testTransactionResponse);

            when(transactionRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                    .thenReturn(transactions);
            when(transactionMapper.toPointTransactionResponseList(transactions))
                    .thenReturn(expectedResponses);

            // When
            List<PointTransactionResponse> result = transactionService.getTransactionsByUser(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(testTransactionId);
        }

        @Test
        @DisplayName("Should return empty list when no transactions")
        void shouldReturnEmptyListWhenNoTransactions() {
            // Given
            when(transactionRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                    .thenReturn(List.of());
            when(transactionMapper.toPointTransactionResponseList(List.of()))
                    .thenReturn(List.of());

            // When
            List<PointTransactionResponse> result = transactionService.getTransactionsByUser(testUserId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Transactions By Type Tests")
    class GetTransactionsByTypeTests {

        @Test
        @DisplayName("Should return credit transactions for user")
        void shouldReturnCreditTransactionsForUser() {
            // Given
            List<PointTransaction> transactions = Arrays.asList(testTransaction);
            List<PointTransactionResponse> expectedResponses = Arrays.asList(testTransactionResponse);

            when(transactionRepository.findByUserIdAndTransactionTypeOrderByCreatedAtDesc(
                    testUserId, TransactionType.CREDIT))
                    .thenReturn(transactions);
            when(transactionMapper.toPointTransactionResponseList(transactions))
                    .thenReturn(expectedResponses);

            // When
            List<PointTransactionResponse> result = transactionService
                    .getTransactionsByUserAndType(testUserId, TransactionType.CREDIT);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Get Recent Transactions Tests")
    class GetRecentTransactionsTests {

        @Test
        @DisplayName("Should return recent transactions for user")
        void shouldReturnRecentTransactionsForUser() {
            // Given
            List<PointTransaction> transactions = Arrays.asList(testTransaction);
            List<PointTransactionResponse> expectedResponses = Arrays.asList(testTransactionResponse);

            when(transactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(testUserId))
                    .thenReturn(transactions);
            when(transactionMapper.toPointTransactionResponseList(transactions))
                    .thenReturn(expectedResponses);

            // When
            List<PointTransactionResponse> result = transactionService.getRecentTransactions(testUserId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(testTransactionId);
        }
    }

    @Nested
    @DisplayName("Get Transactions By Period Tests")
    class GetTransactionsByPeriodTests {

        @Test
        @DisplayName("Should return transactions within period")
        void shouldReturnTransactionsWithinPeriod() {
            // Given
            OffsetDateTime startDate = OffsetDateTime.now().minusDays(7);
            OffsetDateTime endDate = OffsetDateTime.now();
            List<PointTransaction> transactions = Arrays.asList(testTransaction);
            List<PointTransactionResponse> expectedResponses = Arrays.asList(testTransactionResponse);

            when(transactionRepository.findByUserIdAndPeriod(testUserId, startDate, endDate))
                    .thenReturn(transactions);
            when(transactionMapper.toPointTransactionResponseList(transactions))
                    .thenReturn(expectedResponses);

            // When
            List<PointTransactionResponse> result = transactionService
                    .getTransactionsByPeriod(testUserId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Get Transactions By Reference Tests")
    class GetTransactionsByReferenceTests {

        @Test
        @DisplayName("Should return transactions for reference")
        void shouldReturnTransactionsForReference() {
            // Given
            UUID referenceId = UUID.randomUUID();
            String referenceType = "REPORT";
            List<PointTransaction> transactions = Arrays.asList(testTransaction);
            List<PointTransactionResponse> expectedResponses = Arrays.asList(testTransactionResponse);

            when(transactionRepository.findByReferenceIdAndReferenceType(referenceId, referenceType))
                    .thenReturn(transactions);
            when(transactionMapper.toPointTransactionResponseList(transactions))
                    .thenReturn(expectedResponses);

            // When
            List<PointTransactionResponse> result = transactionService
                    .getTransactionsByReference(referenceId, referenceType);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Get Transaction By ID Tests")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("Should return transaction by ID")
        void shouldReturnTransactionById() {
            // Given
            when(transactionRepository.findById(testTransactionId))
                    .thenReturn(Optional.of(testTransaction));
            when(transactionMapper.toPointTransactionResponse(testTransaction))
                    .thenReturn(testTransactionResponse);

            // When
            PointTransactionResponse result = transactionService.getTransactionById(testTransactionId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testTransactionId);
        }

        @Test
        @DisplayName("Should throw exception when transaction not found")
        void shouldThrowExceptionWhenTransactionNotFound() {
            // Given
            when(transactionRepository.findById(testTransactionId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> transactionService.getTransactionById(testTransactionId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Transaction not found");
        }
    }
}

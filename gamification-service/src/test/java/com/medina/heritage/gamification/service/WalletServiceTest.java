package com.medina.heritage.gamification.service;

import com.medina.heritage.gamification.dto.request.AddPointsRequest;
import com.medina.heritage.gamification.dto.request.DeductPointsRequest;
import com.medina.heritage.gamification.dto.response.PointTransactionResponse;
import com.medina.heritage.gamification.dto.response.WalletResponse;
import com.medina.heritage.gamification.entity.PointTransaction;
import com.medina.heritage.gamification.entity.Wallet;
import com.medina.heritage.gamification.enums.TransactionType;
import com.medina.heritage.gamification.exception.DuplicateTransactionException;
import com.medina.heritage.gamification.exception.InsufficientBalanceException;
import com.medina.heritage.gamification.exception.WalletNotFoundException;
import com.medina.heritage.gamification.mapper.PointTransactionMapper;
import com.medina.heritage.gamification.mapper.WalletMapper;
import com.medina.heritage.gamification.repository.PointTransactionRepository;
import com.medina.heritage.gamification.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PointTransactionRepository transactionRepository;

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private PointTransactionMapper transactionMapper;

    @InjectMocks
    private WalletService walletService;

    private UUID testUserId;
    private Wallet testWallet;
    private WalletResponse testWalletResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testWallet = new Wallet();
        testWallet.setUserId(testUserId);
        testWallet.setBalance(100);
        testWallet.setLevel(1);
        testWallet.setTotalEarned(100);
        testWallet.setTotalSpent(0);
        testWallet.setLastUpdatedAt(OffsetDateTime.now());

        testWalletResponse = new WalletResponse();
        testWalletResponse.setUserId(testUserId);
        testWalletResponse.setBalance(100);
        testWalletResponse.setLevel(1);
        testWalletResponse.setTotalEarned(100);
        testWalletResponse.setTotalSpent(0);
        testWalletResponse.setPointsToNextLevel(900);
        testWalletResponse.setProgressPercentage(10);
    }

    @Nested
    @DisplayName("Get or Create Wallet Tests")
    class GetOrCreateWalletTests {

        @Test
        @DisplayName("Should return existing wallet")
        void shouldReturnExistingWallet() {
            // Given
            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.of(testWallet));
            when(walletMapper.toWalletResponse(testWallet)).thenReturn(testWalletResponse);

            // When
            WalletResponse result = walletService.getOrCreateWallet(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(testUserId);
            assertThat(result.getBalance()).isEqualTo(100);
            verify(walletRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create new wallet when not exists")
        void shouldCreateNewWalletWhenNotExists() {
            // Given
            Wallet newWallet = new Wallet(testUserId);
            WalletResponse newWalletResponse = new WalletResponse();
            newWalletResponse.setUserId(testUserId);
            newWalletResponse.setBalance(0);
            newWalletResponse.setLevel(1);

            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);
            when(walletMapper.toWalletResponse(newWallet)).thenReturn(newWalletResponse);

            // When
            WalletResponse result = walletService.getOrCreateWallet(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(testUserId);
            assertThat(result.getBalance()).isEqualTo(0);
            verify(walletRepository).save(any(Wallet.class));
        }
    }

    @Nested
    @DisplayName("Get Wallet Tests")
    class GetWalletTests {

        @Test
        @DisplayName("Should return wallet when exists")
        void shouldReturnWalletWhenExists() {
            // Given
            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.of(testWallet));
            when(walletMapper.toWalletResponse(testWallet)).thenReturn(testWalletResponse);

            // When
            WalletResponse result = walletService.getWallet(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("Should throw exception when wallet not found")
        void shouldThrowExceptionWhenWalletNotFound() {
            // Given
            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> walletService.getWallet(testUserId))
                    .isInstanceOf(WalletNotFoundException.class)
                    .hasMessageContaining(testUserId.toString());
        }
    }

    @Nested
    @DisplayName("Add Points Tests")
    class AddPointsTests {

        @Test
        @DisplayName("Should add points successfully")
        void shouldAddPointsSuccessfully() {
            // Given
            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50);
            request.setReasonCode("REPORT_VALIDATED");
            request.setDescription("Test description");

            PointTransaction savedTransaction = new PointTransaction();
            savedTransaction.setId(UUID.randomUUID());
            savedTransaction.setUserId(testUserId);
            savedTransaction.setPoints(50);
            savedTransaction.setTransactionType(TransactionType.CREDIT);
            savedTransaction.setReasonCode("REPORT_VALIDATED");
            savedTransaction.setBalanceAfter(150);

            PointTransactionResponse transactionResponse = new PointTransactionResponse();
            transactionResponse.setId(savedTransaction.getId());
            transactionResponse.setPoints(50);
            transactionResponse.setTransactionType(TransactionType.CREDIT);

            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.of(testWallet));
            when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
            when(transactionRepository.save(any(PointTransaction.class))).thenReturn(savedTransaction);
            when(transactionMapper.toPointTransactionResponse(savedTransaction)).thenReturn(transactionResponse);

            // When
            PointTransactionResponse result = walletService.addPoints(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPoints()).isEqualTo(50);
            assertThat(result.getTransactionType()).isEqualTo(TransactionType.CREDIT);
            verify(walletRepository).save(any(Wallet.class));
            verify(transactionRepository).save(any(PointTransaction.class));
        }

        @Test
        @DisplayName("Should create wallet when adding points to new user")
        void shouldCreateWalletWhenAddingPointsToNewUser() {
            // Given
            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(100);
            request.setReasonCode("WELCOME_BONUS");

            Wallet newWallet = new Wallet(testUserId);
            PointTransaction savedTransaction = new PointTransaction();
            savedTransaction.setId(UUID.randomUUID());
            PointTransactionResponse transactionResponse = new PointTransactionResponse();

            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);
            when(transactionRepository.save(any(PointTransaction.class))).thenReturn(savedTransaction);
            when(transactionMapper.toPointTransactionResponse(savedTransaction)).thenReturn(transactionResponse);

            // When
            PointTransactionResponse result = walletService.addPoints(request);

            // Then
            assertThat(result).isNotNull();
            verify(walletRepository, times(2)).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Should throw exception for duplicate transaction")
        void shouldThrowExceptionForDuplicateTransaction() {
            // Given
            UUID referenceId = UUID.randomUUID();
            AddPointsRequest request = new AddPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50);
            request.setReasonCode("REPORT_VALIDATED");
            request.setReferenceId(referenceId);
            request.setReferenceType("REPORT");

            when(transactionRepository.existsByReferenceIdAndReferenceTypeAndReasonCode(
                    referenceId, "REPORT", "REPORT_VALIDATED")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> walletService.addPoints(request))
                    .isInstanceOf(DuplicateTransactionException.class);
            verify(walletRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Deduct Points Tests")
    class DeductPointsTests {

        @Test
        @DisplayName("Should deduct points successfully")
        void shouldDeductPointsSuccessfully() {
            // Given
            DeductPointsRequest request = new DeductPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50);
            request.setReasonCode("REWARD_REDEMPTION");

            PointTransaction savedTransaction = new PointTransaction();
            savedTransaction.setId(UUID.randomUUID());
            savedTransaction.setUserId(testUserId);
            savedTransaction.setPoints(50);
            savedTransaction.setTransactionType(TransactionType.DEBIT);
            savedTransaction.setBalanceAfter(50);

            PointTransactionResponse transactionResponse = new PointTransactionResponse();
            transactionResponse.setPoints(50);
            transactionResponse.setTransactionType(TransactionType.DEBIT);

            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.of(testWallet));
            when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
            when(transactionRepository.save(any(PointTransaction.class))).thenReturn(savedTransaction);
            when(transactionMapper.toPointTransactionResponse(savedTransaction)).thenReturn(transactionResponse);

            // When
            PointTransactionResponse result = walletService.deductPoints(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPoints()).isEqualTo(50);
            assertThat(result.getTransactionType()).isEqualTo(TransactionType.DEBIT);
        }

        @Test
        @DisplayName("Should throw exception when wallet not found")
        void shouldThrowExceptionWhenWalletNotFound() {
            // Given
            DeductPointsRequest request = new DeductPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(50);
            request.setReasonCode("REWARD_REDEMPTION");

            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> walletService.deductPoints(request))
                    .isInstanceOf(WalletNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when insufficient balance")
        void shouldThrowExceptionWhenInsufficientBalance() {
            // Given
            DeductPointsRequest request = new DeductPointsRequest();
            request.setUserId(testUserId);
            request.setPoints(200); // More than balance (100)
            request.setReasonCode("REWARD_REDEMPTION");

            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.of(testWallet));

            // When/Then
            assertThatThrownBy(() -> walletService.deductPoints(request))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .hasMessageContaining("200")
                    .hasMessageContaining("100");
        }
    }

    @Nested
    @DisplayName("Balance and Level Tests")
    class BalanceAndLevelTests {

        @Test
        @DisplayName("Should return balance for existing user")
        void shouldReturnBalanceForExistingUser() {
            // Given
            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.of(testWallet));

            // When
            Integer balance = walletService.getBalance(testUserId);

            // Then
            assertThat(balance).isEqualTo(100);
        }

        @Test
        @DisplayName("Should return zero balance for non-existing user")
        void shouldReturnZeroBalanceForNonExistingUser() {
            // Given
            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            // When
            Integer balance = walletService.getBalance(testUserId);

            // Then
            assertThat(balance).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return level for existing user")
        void shouldReturnLevelForExistingUser() {
            // Given
            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.of(testWallet));

            // When
            Integer level = walletService.getLevel(testUserId);

            // Then
            assertThat(level).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return level 1 for non-existing user")
        void shouldReturnLevel1ForNonExistingUser() {
            // Given
            when(walletRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            // When
            Integer level = walletService.getLevel(testUserId);

            // Then
            assertThat(level).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("User Rank Tests")
    class UserRankTests {

        @Test
        @DisplayName("Should return user rank")
        void shouldReturnUserRank() {
            // Given
            when(walletRepository.existsByUserId(testUserId)).thenReturn(true);
            when(walletRepository.getUserRank(testUserId)).thenReturn(5L);

            // When
            Long rank = walletService.getUserRank(testUserId);

            // Then
            assertThat(rank).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should return null for non-existing user")
        void shouldReturnNullForNonExistingUser() {
            // Given
            when(walletRepository.existsByUserId(testUserId)).thenReturn(false);

            // When
            Long rank = walletService.getUserRank(testUserId);

            // Then
            assertThat(rank).isNull();
        }
    }
}

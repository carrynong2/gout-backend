package dev.carrynong.goutbackend.wallet;

import dev.carrynong.goutbackend.booking.model.Booking;
import dev.carrynong.goutbackend.common.enumeration.TransactionType;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.payment.Transaction;
import dev.carrynong.goutbackend.payment.TransactionRepository;
import dev.carrynong.goutbackend.tour.model.Tour;
import dev.carrynong.goutbackend.tour.repository.TourRepository;
import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.wallet.dto.TopupDTO;
import dev.carrynong.goutbackend.wallet.model.TourCompanyWallet;
import dev.carrynong.goutbackend.wallet.model.UserWallet;
import dev.carrynong.goutbackend.wallet.repository.TourCompanyWalletRepository;
import dev.carrynong.goutbackend.wallet.repository.UserWalletRepository;
import dev.carrynong.goutbackend.wallet.service.WalletServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    @InjectMocks
    private WalletServiceImpl walletService;
    @Mock
    private UserWalletRepository userWalletRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TourCompanyWalletRepository tourCompanyWalletRepository;
    @Mock
    private TourRepository tourRepository;


    @Test
    void whenCreateConsumerWalletThenSuccess() {
        AggregateReference<User, Integer> userReference = AggregateReference.to(1);
        Instant currentTimestamp = Instant.now();
        BigDecimal initBalance = new BigDecimal("0.00");
        var mockWallet = new UserWallet(1, userReference, currentTimestamp, initBalance);
        when(userWalletRepository.save(any(UserWallet.class)))
                .thenReturn(mockWallet);
        var actual = walletService.createConsumerWallet(1);
        assertEquals(1, actual.id().intValue());
        assertEquals(userReference, actual.userId());
        assertEquals(currentTimestamp, actual.lastUpdated());
        assertEquals(initBalance, actual.balance());
    }

    @Test
    void whenDeleteConsumerWalletThenSuccess() {
        AggregateReference<User, Integer> userReference = AggregateReference.to(1);
        var mockUserWallet = new UserWallet(1, userReference, Instant.now(), new BigDecimal("0.00"));
        when(userWalletRepository.findByUserId(any(AggregateReference.class)))
                .thenReturn(Optional.of(mockUserWallet));
        doNothing().when(userWalletRepository).delete(any(UserWallet.class));
        Assertions.assertDoesNotThrow(() -> walletService.deleteConsumerWalletByUserId(1));
    }

    @Test
    void whenDeleteConsumerWalletButNotFoundThenFail() {
        when(userWalletRepository.findByUserId(any(AggregateReference.class)))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> walletService.deleteConsumerWalletByUserId(1));
    }

    @Test
    void whenTopupThenSuccess() {
        var userId = 1;
        var amount = new BigDecimal("100.00");
        var topupDTO = new TopupDTO(amount, userId, "uniqueKey");
        AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
        var userWallet = new UserWallet(1, userReference, Instant.now(), BigDecimal.ZERO);
        var updatedUserWallet = new UserWallet(1, userReference, Instant.now(), amount);

        when(userWalletRepository.findByUserId(any(AggregateReference.class))).thenReturn(Optional.of(userWallet));
        when(transactionRepository.findByIdempotentKey(anyString())).thenReturn(Optional.empty());
        when(userWalletRepository.save(any(UserWallet.class))).thenReturn(updatedUserWallet);

        var result = walletService.topup(topupDTO);

        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertEquals(amount, result.balance());
        verify(userWalletRepository, times(1)).findByUserId(any(AggregateReference.class));
        verify(transactionRepository, times(1)).findByIdempotentKey(anyString());
        verify(userWalletRepository, times(1)).save(any(UserWallet.class));
    }

//    @Test
//    void whenTopupWithExistingIdempotentKeyThenReturnExistingWalletInfo() {
//        var userId = 1;
//        var amount = new BigDecimal("100.00");
//        var topupDTO = new TopupDTO(amount, userId, "uniqueKey");
//        AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
//        var userWallet = new UserWallet(1, userReference, Instant.now(), BigDecimal.ZERO);
//
//        when(userWalletRepository.findByUserId(any(AggregateReference.class))).thenReturn(Optional.of(userWallet));
//        when(transactionRepository.findByIdempotentKey(anyString())).thenReturn(Optional.of(new Transaction()));
//
//        var result = walletService.topup(topupDTO);
//
//        assertNotNull(result);
//        assertEquals(userId, result.userId());
//        assertEquals(BigDecimal.ZERO, result.balance());
//        verify(userWalletRepository, times(1)).findByUserId(any(AggregateReference.class));
//        verify(transactionRepository, times(1)).findByIdempotentKey(anyString());
//        verify(userWalletRepository, times(0)).save(any(UserWallet.class));
//    }

    @Test
    void whenGetOwnWalletThenReturnWalletInfo() {
        var userId = 1;
        AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
        var userWallet = new UserWallet(1, userReference, Instant.now(), new BigDecimal("100.00"));

        when(userWalletRepository.findByUserId(any(AggregateReference.class))).thenReturn(Optional.of(userWallet));

        var result = walletService.getOwnWallet(userId);

        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertEquals(new BigDecimal("100.00"), result.balance());
        verify(userWalletRepository, times(1)).findByUserId(any(AggregateReference.class));
    }

//    @Test
//    void whenGetUserWalletAndTourCompanyWalletThenSuccess() {
//        AggregateReference<User, Integer> userId = AggregateReference.to(1);
//        AggregateReference<Tour, Integer> tourId = AggregateReference.to(2);
//        var bookingData = new Booking(null, userId, tourId);
//
//        AggregateReference<User, Integer> userReference = AggregateReference.to(1);
//        AggregateReference<TourCompany, Integer> tourCompanyReference = AggregateReference.to(3);
//
//        var userWallet = new UserWallet(1, userReference, Instant.now(), new BigDecimal("100.00"));
//        var tourCompanyWallet = new TourCompanyWallet(1, tourCompanyReference, Instant.now(), new BigDecimal("200.00"));
//        var tourInfo = new Tour(tourId.getId(), tourCompanyReference, "Sample Tour");
//
//        when(userWalletRepository.findByUserId(any(AggregateReference.class))).thenReturn(Optional.of(userWallet));
//        when(tourRepository.findById(anyInt())).thenReturn(Optional.of(tourInfo));
//        when(tourCompanyWalletRepository.findOneByTourCompanyId(any(AggregateReference.class)))
//                .thenReturn(Optional.of(tourCompanyWallet));
//
//        var result = walletService.getUserWalletAndTourCompanyWallet(bookingData);
//
//        assertNotNull(result);
//        assertEquals(userWallet, result.getFirst());
//        assertEquals(tourCompanyWallet, result.getSecond());
//        verify(userWalletRepository, times(1)).findByUserId(any(AggregateReference.class));
//        verify(tourRepository, times(1)).findById(anyInt());
//        verify(tourCompanyWalletRepository, times(1)).findOneByTourCompanyId(any(AggregateReference.class));
//    }

    @Test
    void whenTransferBookingThenSuccess() {
        var userId = 1;
        AggregateReference<TourCompany, Integer> tourCompanyId = AggregateReference.to(3);
        var userWallet = new UserWallet(1, AggregateReference.to(userId), Instant.now(), new BigDecimal("100.00"));
        var tourCompanyWallet = new TourCompanyWallet(1, tourCompanyId, Instant.now(), new BigDecimal("200.00"));
        var amount = new BigDecimal("50.00");

        when(userWalletRepository.save(any(UserWallet.class))).thenReturn(userWallet);
        when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class))).thenReturn(tourCompanyWallet);

        var result = walletService.transfer(userWallet, tourCompanyWallet, amount, TransactionType.BOOKING);

        assertNotNull(result);
        assertEquals(userWallet, result.getFirst());
        assertEquals(tourCompanyWallet, result.getSecond());
        verify(userWalletRepository, times(1)).save(any(UserWallet.class));
        verify(tourCompanyWalletRepository, times(1)).save(any(TourCompanyWallet.class));
    }

    @Test
    void whenTransferRefundThenSuccess() {
        var userId = 1;
        AggregateReference<TourCompany, Integer> tourCompanyId = AggregateReference.to(2);
        var userWallet = new UserWallet(1, AggregateReference.to(userId), Instant.now(), new BigDecimal("50.00"));
        var tourCompanyWallet = new TourCompanyWallet(1, tourCompanyId, Instant.now(), new BigDecimal("100.00"));
        var amount = new BigDecimal("50.00");

        when(userWalletRepository.save(any(UserWallet.class))).thenReturn(userWallet);
        when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class))).thenReturn(tourCompanyWallet);

        var result = walletService.transfer(userWallet, tourCompanyWallet, amount, TransactionType.REFUND);

        assertNotNull(result);
        assertEquals(userWallet, result.getFirst());
        assertEquals(tourCompanyWallet, result.getSecond());
        verify(userWalletRepository, times(1)).save(any(UserWallet.class));
        verify(tourCompanyWalletRepository, times(1)).save(any(TourCompanyWallet.class));
    }
}


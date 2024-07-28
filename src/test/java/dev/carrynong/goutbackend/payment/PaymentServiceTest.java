package dev.carrynong.goutbackend.payment;

import dev.carrynong.goutbackend.booking.dto.BookingInfoDto;
import dev.carrynong.goutbackend.booking.model.Booking;
import dev.carrynong.goutbackend.booking.repository.BookingRepository;
import dev.carrynong.goutbackend.common.enumeration.BookingStatusEnum;
import dev.carrynong.goutbackend.common.enumeration.QrCodeStatus;
import dev.carrynong.goutbackend.common.enumeration.TransactionType;
import dev.carrynong.goutbackend.qrcode.QrCodeService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.util.Pair;
import dev.carrynong.goutbackend.qrcode.QrCodeReference;
import dev.carrynong.goutbackend.tour.service.TourCountService;
import dev.carrynong.goutbackend.wallet.service.WalletService;
import dev.carrynong.goutbackend.wallet.model.UserWallet;
import dev.carrynong.goutbackend.wallet.model.TourCompanyWallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @InjectMocks
    private PaymentServiceImpl paymentService;
    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private WalletService walletService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TourCountService tourCountService;
    @Value("${booking.tour-price}")
    private int tourPrice = 100; // Set default value or use @Mock for this as well

    @Test
    void whenGeneratePaymentQrThenReturnBufferedImage() throws Exception {
        int qrId = 1;
        BufferedImage mockImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        when(qrCodeService.generateQrById(qrId)).thenReturn(mockImage);

        BufferedImage result = paymentService.generatePaymentQr(qrId);

        assertNotNull(result);
        assertEquals(mockImage, result);
    }

    @Test
    @Transactional
    void whenPaymentOnBookingThenReturnBookingInfoDto() {
        int bookingId = 1;
        String idempotentKey = "uniqueKey";
        int userId = 1;
        int tourCompanyId = 2;
        var userWallet = new UserWallet(userId, AggregateReference.to(userId), Instant.now(), BigDecimal.valueOf(1000));
        var tourCompanyWallet = new TourCompanyWallet(tourCompanyId, AggregateReference.to(tourCompanyId), Instant.now(), BigDecimal.valueOf(1000));
        var booking = new Booking(bookingId, AggregateReference.to(userId), AggregateReference.to(tourCompanyId), BookingStatusEnum.PENDING.name(), Instant.now(), Instant.now(), idempotentKey);
        var qrCodeReference = new QrCodeReference(1, bookingId, "content", QrCodeStatus.ACTIVATED.name());
        var mockTransaction = new Transaction(
                1, // id
                AggregateReference.to(userId), // userId
                AggregateReference.to(tourCompanyId), // tourCompanyId
                Instant.now(), // transactionDate
                BigDecimal.valueOf(tourPrice), // amount
                TransactionType.BOOKING.name(), // type
                idempotentKey, // idempotentKey
                bookingId // bookingId
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(walletService.getUserWalletAndTourCompanyWallet(booking)).thenReturn(Pair.of(userWallet, tourCompanyWallet));
        when(walletService.transfer(userWallet, tourCompanyWallet, BigDecimal.valueOf(tourPrice), TransactionType.BOOKING))
                .thenReturn(Pair.of(userWallet, tourCompanyWallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(qrCodeService.updateQrStatus(bookingId, QrCodeStatus.EXPIRED)).thenReturn(qrCodeReference);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingInfoDto result = paymentService.paymentOnBooking(idempotentKey, bookingId);

        assertNotNull(result);
        assertEquals(bookingId, result.bookingId());
        assertEquals(userId, result.userId());
        assertEquals(tourCompanyId, result.tourId());
        assertEquals(BookingStatusEnum.COMPLETED.name(), result.state());
        assertEquals(1, result.qrReference());
        verify(tourCountService, times(1)).incrementTourCount(anyInt());
    }

    @Test
    @Transactional
    void whenRefundOnBookingThenSuccess() {
        int bookingId = 1;
        String idempotentKey = "uniqueKey";
        var userId = 1;
        var tourCompanyId = 1;
        var userWallet = new UserWallet(userId, AggregateReference.to(userId), Instant.now(), BigDecimal.valueOf(1000));
        var tourCompanyWallet = new TourCompanyWallet(tourCompanyId, AggregateReference.to(tourCompanyId), Instant.now(), BigDecimal.valueOf(1000));
        var booking = new Booking(bookingId, null, null, BookingStatusEnum.PENDING.name(), Instant.now(), Instant.now(), idempotentKey);
        var mockTransaction = new Transaction(
                1, // id
                AggregateReference.to(userId), // userId
                AggregateReference.to(tourCompanyId), // tourCompanyId
                Instant.now(), // transactionDate
                BigDecimal.valueOf(tourPrice), // amount
                TransactionType.REFUND.name(), // type
                idempotentKey, // idempotentKey
                bookingId // bookingId
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(walletService.getUserWalletAndTourCompanyWallet(booking)).thenReturn(Pair.of(userWallet, tourCompanyWallet));
        when(walletService.transfer(userWallet, tourCompanyWallet, BigDecimal.valueOf(tourPrice), TransactionType.REFUND))
                .thenReturn(Pair.of(userWallet, tourCompanyWallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        assertDoesNotThrow(() -> paymentService.refundOnBooking(idempotentKey, bookingId));
    }
}
package dev.carrynong.goutbackend.payment;

import dev.carrynong.goutbackend.booking.dto.BookingInfoDto;
import dev.carrynong.goutbackend.booking.model.Booking;
import dev.carrynong.goutbackend.booking.repository.BookingRepository;
import dev.carrynong.goutbackend.common.enumeration.BookingStatusEnum;
import dev.carrynong.goutbackend.common.enumeration.QrCodeStatus;
import dev.carrynong.goutbackend.common.enumeration.TransactionType;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.qrcode.QrCodeService;
import dev.carrynong.goutbackend.tour.service.TourCountService;
import dev.carrynong.goutbackend.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.Instant;

@Service
public class PaymentServiceImpl implements PaymentService{
    private final QrCodeService qrCodeService;
    private final BookingRepository bookingRepository;
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final TourCountService tourCountService;
    private final int tourPrice;

//    public PaymentServiceImpl(QrCodeService qrCodeService, BookingRepository bookingRepository,
//                              WalletService walletService, TransactionRepository transactionRepository,
//                              TourCountService tourCountService, @Value(value = "${booking.tour-price}") int tourPrice) {
//        this.qrCodeService = qrCodeService;
//        this.bookingRepository = bookingRepository;
//        this.walletService = walletService;
//        this.transactionRepository = transactionRepository;
//        this.tourCountService = tourCountService;
//        this.tourPrice = tourPrice;
//    }

//     New constructor for testing
    public PaymentServiceImpl(QrCodeService qrCodeService, BookingRepository bookingRepository,
                              WalletService walletService, TransactionRepository transactionRepository,
                              TourCountService tourCountService) {
        this.qrCodeService = qrCodeService;
        this.bookingRepository = bookingRepository;
        this.walletService = walletService;
        this.transactionRepository = transactionRepository;
        this.tourCountService = tourCountService;
        this.tourPrice = 100; // Default value for testing
    }

    @Override
    public BufferedImage generatePaymentQr(int id) throws Exception {
        return qrCodeService.generateQrById(id);
    }

    @Override
    @Transactional
    public BookingInfoDto paymentOnBooking(String idempotentKey, int bookingId) {
        // idempotentKey - use in transaction
        var bookingData = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("BookingId: %d not found", bookingId)));
        var wallets = walletService.getUserWalletAndTourCompanyWallet(bookingData);
        var userWallet = wallets.getFirst();
        var tourCompanyWallet = wallets.getSecond();
        // UserWallet -
        // TourCompanyWallet +
        walletService.transfer(userWallet, tourCompanyWallet, BigDecimal.valueOf(tourPrice), TransactionType.BOOKING);
        var newTransaction = TransactionUtil.generateBookingTransaction(idempotentKey, bookingId,
                            userWallet.userId().getId(), tourCompanyWallet.tourCompanyId().getId(), Instant.now(), BigDecimal.valueOf(tourPrice));
        transactionRepository.save(newTransaction);
        var qrCodeReference = qrCodeService.updateQrStatus(bookingId, QrCodeStatus.EXPIRED);
        var prepareUpdateBooking = new Booking(bookingData.id(), bookingData.userId(), bookingData.tourId(),
                BookingStatusEnum.COMPLETED.name(), bookingData.bookingDate(), Instant.now(), idempotentKey);
        bookingRepository.save(prepareUpdateBooking);
        // Update tour count
        tourCountService.incrementTourCount(bookingData.tourId().getId());
        return new BookingInfoDto(bookingData.id(), bookingData.userId().getId(), bookingData.tourId().getId(),
                BookingStatusEnum.COMPLETED.name(), qrCodeReference.id());
    }

    @Override
    @Transactional
    public void refundOnBooking(String idempotentKey, int bookingId) {
        var bookingData = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("BookingId: %d not found", bookingId)));
        var wallets = walletService.getUserWalletAndTourCompanyWallet(bookingData);
        var userWallet = wallets.getFirst();
        var tourCompanyWallet = wallets.getSecond();
        // TourCompanyWallet -
        // UserWallet +
        walletService.transfer(
                userWallet,
                tourCompanyWallet,
                BigDecimal.valueOf(tourPrice),
                TransactionType.REFUND);
        var newTransaction = TransactionUtil.generateRefundTransaction(
                idempotentKey,
                bookingId,
                userWallet.userId().getId(),
                tourCompanyWallet.tourCompanyId().getId(),
                Instant.now(),
                BigDecimal.valueOf(tourPrice));
        transactionRepository.save(newTransaction);
    }
}

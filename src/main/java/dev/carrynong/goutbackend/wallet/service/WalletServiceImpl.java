package dev.carrynong.goutbackend.wallet.service;

import dev.carrynong.goutbackend.booking.model.Booking;
import dev.carrynong.goutbackend.common.enumeration.TransactionType;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.payment.TransactionUtil;
import dev.carrynong.goutbackend.tour.repository.TourRepository;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.wallet.dto.TopupDTO;
import dev.carrynong.goutbackend.wallet.dto.UserWalletInfoDTO;
import dev.carrynong.goutbackend.wallet.model.TourCompanyWallet;
import dev.carrynong.goutbackend.wallet.model.UserWallet;
import dev.carrynong.goutbackend.payment.TransactionRepository;
import dev.carrynong.goutbackend.wallet.repository.TourCompanyWalletRepository;
import dev.carrynong.goutbackend.wallet.repository.UserWalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class WalletServiceImpl implements WalletService {
    private final UserWalletRepository userWalletRepository;
    private final TransactionRepository transactionRepository;
    private final TourCompanyWalletRepository tourCompanyWalletRepository;
    private final TourRepository tourRepository;
    private final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    public WalletServiceImpl(UserWalletRepository userWalletRepository, TransactionRepository transactionRepository, TourCompanyWalletRepository tourCompanyWalletRepository, TourRepository tourRepository) {
        this.userWalletRepository = userWalletRepository;
        this.transactionRepository = transactionRepository;
        this.tourCompanyWalletRepository = tourCompanyWalletRepository;
        this.tourRepository = tourRepository;
    }

    @Override
    @Transactional
    public UserWallet createConsumerWallet(int userId) {
        AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
        Instant currentTimestamp = Instant.now();
        BigDecimal initBalance = new BigDecimal("0.00");
        var wallet = new UserWallet(null, userReference, currentTimestamp, initBalance);
        var newWallet = userWalletRepository.save(wallet);
        logger.info("Created wallet for user: {}", userId);
        return newWallet;
    }

    @Override
    @Transactional
    public void deleteConsumerWalletByUserId(int userId) {
        AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
        var wallet = userWalletRepository.findByUserId(userReference)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Wallet for User Id: %s not found", userId)));
        userWalletRepository.delete(wallet);
    }

    @Override
    public UserWalletInfoDTO topup(TopupDTO body) {
        var now = Instant.now();
        var idempotentKey = body.idempotentKey();
        var userId = body.userId();
        var userWallet = getWalletByUserId(userId);
        var optionalHistoricalTransaction = transactionRepository.findByIdempotentKey(idempotentKey);
        // If Idempotent key exists -> Just return existing information
        if (optionalHistoricalTransaction.isPresent()) {
            return new UserWalletInfoDTO(userWallet.userId().getId(), userWallet.balance());
        }
        var newTransaction = TransactionUtil.generateTopupTransaction(idempotentKey, userId, now, body.amount());
        transactionRepository.save(newTransaction);
        var updatedBalance = userWallet.balance().add(body.amount());
        var updatedTopupBalance = new UserWallet(userWallet.id(), userWallet.userId(), now, updatedBalance);
        var updatedWallet = userWalletRepository.save(updatedTopupBalance);
        return new UserWalletInfoDTO(updatedWallet.userId().getId(), updatedWallet.balance());
    }

    @Override
    public Pair<UserWallet, TourCompanyWallet> getUserWalletAndTourCompanyWallet(Booking bookingData) {
        var userId = bookingData.userId();
        var tourId = bookingData.tourId();
        var userWallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(EntityNotFoundException::new);
        var tourInfo = tourRepository.findById(tourId.getId())
                .orElseThrow(EntityNotFoundException::new);
        var tourCompanyWallet = tourCompanyWalletRepository.findOneByTourCompanyId(tourInfo.tourCompanyId())
                .orElseThrow(EntityNotFoundException::new);
        return Pair.of(userWallet, tourCompanyWallet);
    }

    @Override
    public Pair<UserWallet, TourCompanyWallet> transfer(UserWallet userWallet,
                                                        TourCompanyWallet tourCompanyWallet,
                                                        BigDecimal amount,
                                                        TransactionType type) {
        return switch (type) {
            case TransactionType.BOOKING -> {
                var prepareUserWallet = new UserWallet(
                        userWallet.id(),
                        userWallet.userId(),
                        Instant.now(),
                        userWallet.balance().subtract(amount)
                );
                var prepaTourCompanyWallet = new TourCompanyWallet(
                        tourCompanyWallet.id(),
                        tourCompanyWallet.tourCompanyId(),
                        Instant.now(),
                        tourCompanyWallet.balance().add(amount)
                );
                var updateUserWallet = userWalletRepository.save(prepareUserWallet);
                var updateTourCompanyWallet = tourCompanyWalletRepository.save(prepaTourCompanyWallet);
                yield Pair.of(updateUserWallet, updateTourCompanyWallet);
            }
            case TransactionType.REFUND -> {
                var prepareUserWallet = new UserWallet(
                        userWallet.id(),
                        userWallet.userId(),
                        Instant.now(),
                        userWallet.balance().add(amount)
                );
                var prepaTourCompanyWallet = new TourCompanyWallet(
                        tourCompanyWallet.id(),
                        tourCompanyWallet.tourCompanyId(),
                        Instant.now(),
                        tourCompanyWallet.balance().subtract(amount)
                );
                var updateUserWallet = userWalletRepository.save(prepareUserWallet);
                var updateTourCompanyWallet = tourCompanyWalletRepository.save(prepaTourCompanyWallet);
                yield Pair.of(updateUserWallet, updateTourCompanyWallet);
            }
            default -> {
                throw new IllegalArgumentException("Invalid Transaction Type");
            }
        };
    }

    @Override
    public UserWalletInfoDTO getOwnWallet(int userId) {
        var userWallet = getWalletByUserId(userId);
        return new UserWalletInfoDTO(userWallet.userId().getId(), userWallet.balance());
    }

    private UserWallet getWalletByUserId(int userId) {
        return userWalletRepository.findByUserId(AggregateReference.to(userId))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Wallet for userId: %d not found", userId)));
    }


}

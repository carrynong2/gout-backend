package dev.carrynong.goutbackend.wallet.service;

import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.wallet.model.UserWallet;
import dev.carrynong.goutbackend.wallet.repository.UserWalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class WalletServiceImpl implements WalletService {
    private final UserWalletRepository userWalletRepository;
    private final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    public WalletServiceImpl(UserWalletRepository userWalletRepository) {
        this.userWalletRepository = userWalletRepository;
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

}

package dev.carrynong.goutbackend.wallet;

import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.wallet.model.UserWallet;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    @InjectMocks
    private WalletServiceImpl walletService;
    @Mock
    private UserWalletRepository userWalletRepository;

    @Test
    void whenCreateConsumerWalletThenSuccess() {
        AggregateReference<User, Integer> userReference = AggregateReference.to(1);
        Instant currentTimestamp = Instant.now();
        BigDecimal initBalance = new BigDecimal("0.00");
        var mockWallet = new UserWallet(1, userReference, currentTimestamp, initBalance);
        when(userWalletRepository.save(any(UserWallet.class)))
                .thenReturn(mockWallet);
        var actual = walletService.createConsumerWallet(1);
        Assertions.assertEquals(1, actual.id().intValue());
        Assertions.assertEquals(userReference, actual.userId());
        Assertions.assertEquals(currentTimestamp, actual.lastUpdated());
        Assertions.assertEquals(initBalance, actual.balance());
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
}


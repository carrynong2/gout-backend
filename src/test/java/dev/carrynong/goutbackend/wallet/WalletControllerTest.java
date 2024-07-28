package dev.carrynong.goutbackend.wallet;

import dev.carrynong.goutbackend.wallet.controller.WalletController;
import dev.carrynong.goutbackend.wallet.dto.TopupDTO;
import dev.carrynong.goutbackend.wallet.dto.UserWalletInfoDTO;
import dev.carrynong.goutbackend.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletControllerTest {
    @InjectMocks
    private WalletController walletController;
    @Mock
    private WalletService walletService;
    @Mock
    private Authentication authentication;
    @Mock
    private Jwt jwt;

    @Test
    void whenGetOwnWalletThenReturnWalletInfo() {
        var userId = 1;
        var userWalletInfo = new UserWalletInfoDTO(userId, new BigDecimal("100.00"));

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn(String.valueOf(userId));
        when(walletService.getOwnWallet(userId)).thenReturn(userWalletInfo);

        ResponseEntity<UserWalletInfoDTO> response = walletController.getOwnWallet(authentication);

        assertEquals(ResponseEntity.ok(userWalletInfo), response);
        verify(walletService, times(1)).getOwnWallet(userId);
    }

    @Test
    void whenTopupThenReturnUpdatedWalletInfo() {
        var userId = 1;
        var amount = new BigDecimal("100.00");
        var idempotentKey = "uniqueKey";
        var topupDTO = new TopupDTO(amount, userId, idempotentKey);
        var userWalletInfo = new UserWalletInfoDTO(userId, amount);

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn(String.valueOf(userId));
        when(walletService.topup(any(TopupDTO.class))).thenReturn(userWalletInfo);

        ResponseEntity<UserWalletInfoDTO> response = walletController.topup(authentication, topupDTO, idempotentKey);

        assertEquals(ResponseEntity.ok(userWalletInfo), response);
        verify(walletService, times(1)).topup(any(TopupDTO.class));
    }
}
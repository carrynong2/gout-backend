package dev.carrynong.goutbackend.wallet.controller;

import dev.carrynong.goutbackend.wallet.dto.TopupDTO;
import dev.carrynong.goutbackend.wallet.dto.TourCompanyWalletInfoDTO;
import dev.carrynong.goutbackend.wallet.dto.UserWalletInfoDTO;
import dev.carrynong.goutbackend.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {
    private final Logger logger = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // User -> See own wallet
    @GetMapping("/me")
    public ResponseEntity<UserWalletInfoDTO> getOwnWallet(Authentication authentication) {
        var jwt = (Jwt) authentication.getPrincipal();
        var userId = jwt.getClaimAsString("sub");
        var wallet = walletService.getOwnWallet(Integer.valueOf(userId));
        return ResponseEntity.ok(wallet);
    }
    // User -> Topup
    @PostMapping("/topup")
    public ResponseEntity<UserWalletInfoDTO> topup(Authentication authentication,
                                                   @RequestBody @Validated TopupDTO body,
                                                   @RequestHeader("idempotent-key") String idempotentKey) {
        var jwt = (Jwt) authentication.getPrincipal();
        var userId = jwt.getClaimAsString("sub");
        var recreateBody = new TopupDTO(body.amount(), Integer.valueOf(userId) ,idempotentKey);
        var result = walletService.topup(recreateBody);
        return ResponseEntity.ok(result);
    }
//    // Company -> See own wallet
//    public ResponseEntity<TourCompanyWalletInfoDTO> getOwnWalletForCompany() {
//        return ResponseEntity.ok().build();
//    }
//    // Company -> pay to bank account
//    public ResponseEntity<TourCompanyWalletInfoDTO> payToOwnBankAccount() {
//        return ResponseEntity.ok().build();
//    }
}

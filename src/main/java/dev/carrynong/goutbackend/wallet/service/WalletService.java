package dev.carrynong.goutbackend.wallet.service;

import dev.carrynong.goutbackend.wallet.model.UserWallet;

public interface WalletService {
    UserWallet createConsumerWallet(int userId);
    void deleteConsumerWalletByUserId(int userId);
}

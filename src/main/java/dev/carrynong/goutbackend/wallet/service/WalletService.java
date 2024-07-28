package dev.carrynong.goutbackend.wallet.service;

import dev.carrynong.goutbackend.booking.model.Booking;
import dev.carrynong.goutbackend.common.enumeration.TransactionType;
import dev.carrynong.goutbackend.wallet.dto.TopupDTO;
import dev.carrynong.goutbackend.wallet.dto.UserWalletInfoDTO;
import dev.carrynong.goutbackend.wallet.model.TourCompanyWallet;
import dev.carrynong.goutbackend.wallet.model.UserWallet;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;

public interface WalletService {
    UserWallet createConsumerWallet(int userId);
    void deleteConsumerWalletByUserId(int userId);
    UserWalletInfoDTO getOwnWallet(int userId);
    UserWalletInfoDTO topup(TopupDTO body);
    Pair<UserWallet, TourCompanyWallet> getUserWalletAndTourCompanyWallet(Booking bookingData);
    Pair<UserWallet, TourCompanyWallet> transfer(
            UserWallet userWallet,
            TourCompanyWallet tourCompanyWallet,
            BigDecimal amount,
            TransactionType type
    );
}

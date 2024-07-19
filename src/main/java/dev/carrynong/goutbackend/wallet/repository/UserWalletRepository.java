package dev.carrynong.goutbackend.wallet.repository;

import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.wallet.model.UserWallet;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserWalletRepository extends CrudRepository<UserWallet, Integer> {
    Optional<UserWallet> findByUserId(AggregateReference<User, Integer> userId);
}

package dev.carrynong.goutbackend.wallet.model;

import dev.carrynong.goutbackend.user.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table(name = "user_wallet")
public record UserWallet(
        @Id Integer id,
        AggregateReference<User, Integer> userId,
        Instant lastUpdated,
        BigDecimal balance
) {
}

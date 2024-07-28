package dev.carrynong.goutbackend.payment;

import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.user.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table(name = "transaction")
public record Transaction(
        @Id Integer id,
        AggregateReference<User, Integer> userId,
        AggregateReference<TourCompany, Integer> tourCompanyId,
        Instant transactionDate,
        BigDecimal amount,
        String type,
        String idempotentKey,
        Integer bookingId
) {
}

package dev.carrynong.goutbackend.wallet.model;

import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table(name = "tour_company_wallet")
public record TourCompanyWallet(
        @Id Integer id,
        AggregateReference<TourCompany, Integer> tourCompanyId,
        Instant lastUpdated,
        BigDecimal balance) {
}

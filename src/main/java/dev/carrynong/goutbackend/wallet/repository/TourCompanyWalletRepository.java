package dev.carrynong.goutbackend.wallet.repository;

import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.wallet.model.TourCompanyWallet;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TourCompanyWalletRepository extends CrudRepository<TourCompanyWallet, Integer> {
    Optional<TourCompanyWallet> findOneByTourCompanyId(AggregateReference<TourCompany, Integer> tourCompanyId);
}

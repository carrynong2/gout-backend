package dev.carrynong.goutbackend.tourcompany.repositoriy;

import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.tourcompany.model.TourCompanyLogin;
import dev.carrynong.goutbackend.wallet.model.TourCompanyWallet;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TourCompanyRepository extends CrudRepository<TourCompany, Integer> {
}

package dev.carrynong.goutbackend.tourcompany.repositoriy;

import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.tourcompany.model.TourCompanyLogin;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TourCompanyLoginRepository extends CrudRepository<TourCompanyLogin, Integer> {
    Optional<TourCompanyLogin> findOneByUsername(String username);
    Optional<TourCompanyLogin> findOneByTourCompanyId(AggregateReference<TourCompany, Integer> tourCompanyId);
}

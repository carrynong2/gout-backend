package dev.carrynong.goutbackend.tourcompany.repositoriy;

import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.tourcompany.model.TourCompanyLogin;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TourCompanyRepository extends CrudRepository<TourCompany, Integer> {
}

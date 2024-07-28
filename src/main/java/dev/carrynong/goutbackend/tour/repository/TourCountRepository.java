package dev.carrynong.goutbackend.tour.repository;

import dev.carrynong.goutbackend.tour.model.Tour;
import dev.carrynong.goutbackend.tour.model.TourCount;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TourCountRepository extends CrudRepository<TourCount, Integer> {
    @Lock(LockMode.PESSIMISTIC_WRITE)
    Optional<TourCount> findOneByTourId(AggregateReference<Tour, Integer> tourId);
}

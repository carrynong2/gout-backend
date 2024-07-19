package dev.carrynong.goutbackend.tour.repository;

import dev.carrynong.goutbackend.tour.model.TourCount;
import org.springframework.data.repository.CrudRepository;

public interface TourCountRepository extends CrudRepository<TourCount, Integer> {
}

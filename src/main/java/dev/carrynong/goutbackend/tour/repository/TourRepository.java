package dev.carrynong.goutbackend.tour.repository;

import dev.carrynong.goutbackend.tour.model.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

public interface TourRepository extends ListCrudRepository<Tour, Integer> {
    Page<Tour> findAll(Pageable pageable);
}

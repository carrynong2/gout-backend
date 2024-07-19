package dev.carrynong.goutbackend.tour.service;

import dev.carrynong.goutbackend.tour.dto.TourDTO;
import dev.carrynong.goutbackend.tour.model.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TourService {
    Tour createTour(TourDTO payload);
    Tour getTourById(int id);
    Page<Tour> getPageTour(Pageable pageable);
}

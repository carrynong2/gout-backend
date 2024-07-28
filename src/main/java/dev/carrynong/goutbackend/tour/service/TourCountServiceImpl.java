package dev.carrynong.goutbackend.tour.service;

import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.tour.model.TourCount;
import dev.carrynong.goutbackend.tour.repository.TourCountRepository;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

@Service
public class TourCountServiceImpl implements TourCountService{
    private final TourCountRepository tourCountRepository;

    public TourCountServiceImpl(TourCountRepository tourCountRepository) {
        this.tourCountRepository = tourCountRepository;
    }

    @Override
    public void incrementTourCount(int tourId) {
        var tourCount = tourCountRepository.findOneByTourId(AggregateReference.to(tourId))
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("TourCount for tourId: %d not found", tourId)));
        var newAmount = tourCount.amount() + 1;
        var prepareTourCount = new TourCount(tourCount.id(), tourCount.tourId(), newAmount);
        tourCountRepository.save(prepareTourCount);
    }

    @Override
    public void decrementTourCount(int tourId) {
        var tourCount = tourCountRepository.findOneByTourId(AggregateReference.to(tourId))
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("TourCount for tourId: %d not found", tourId)));
        var newAmount = tourCount.amount() - 1;
        var prepareTourCount = new TourCount(tourCount.id(), tourCount.tourId(), newAmount);
        tourCountRepository.save(prepareTourCount);
    }
}

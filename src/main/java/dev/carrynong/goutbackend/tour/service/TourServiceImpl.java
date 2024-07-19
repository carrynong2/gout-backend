package dev.carrynong.goutbackend.tour.service;

import dev.carrynong.goutbackend.common.enumeration.TourStatus;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.tour.model.TourCount;
import dev.carrynong.goutbackend.tour.repository.TourCountRepository;
import dev.carrynong.goutbackend.tour.repository.TourRepository;
import dev.carrynong.goutbackend.tour.dto.TourDTO;
import dev.carrynong.goutbackend.tour.model.Tour;
import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.tourcompany.repositoriy.TourCompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TourServiceImpl implements TourService {
    private final Logger logger = LoggerFactory.getLogger(TourServiceImpl.class);
    private final TourRepository tourRepository;
    private final TourCompanyRepository tourCompanyRepository;
    private final TourCountRepository tourCountRepository;

    public TourServiceImpl(TourRepository tourRepository,
                           TourCompanyRepository tourCompanyRepository,
                           TourCountRepository tourCountRepository) {
        this.tourRepository = tourRepository;
        this.tourCompanyRepository = tourCompanyRepository;
        this.tourCountRepository = tourCountRepository;
    }

    @Override
    @Transactional
    public Tour createTour(TourDTO payload) {
        var tourCompanyId = payload.tourCompanyId();
        var tourCompany = tourCompanyRepository.findById(tourCompanyId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Tour Company Id: %s not found", tourCompanyId)));
        AggregateReference<TourCompany, Integer> tourCompanyReference = AggregateReference.to(tourCompany.id());
        var tour = new Tour(null, tourCompanyReference, payload.title(),
                payload.description(), payload.location(), 0,
                payload.activityDate(), TourStatus.PENDING.name());
        var newTour = tourRepository.save(tour);
        logger.debug("Tour has been created: {}", tour);
        tourCountRepository.save(new TourCount(null, AggregateReference.to(newTour.id()), 0));
        return newTour;
    }

    @Override
    public Tour getTourById(int id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Tour Id: %d not found" ,id)));
    }

    @Override
    public Page<Tour> getPageTour(Pageable pageable) {
        return tourRepository.findAll(pageable);
    }
}

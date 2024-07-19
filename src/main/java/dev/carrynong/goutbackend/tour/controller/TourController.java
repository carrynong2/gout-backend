package dev.carrynong.goutbackend.tour.controller;

import dev.carrynong.goutbackend.tour.dto.TourDTO;
import dev.carrynong.goutbackend.tour.model.Tour;
import dev.carrynong.goutbackend.tour.service.TourService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tours")
public class TourController {
    private final Logger logger = LoggerFactory.getLogger(TourController.class);
    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping("")
    public Page<Tour> getTours(
            @RequestParam(required = true) int page,
            @RequestParam(required = true) int size,
            @RequestParam(required = true) String sortField,
            @RequestParam(required = true) String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.valueOf(sortDirection.toUpperCase()), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return tourService.getPageTour(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tour> getToursById(@PathVariable int id) {
        logger.debug("Get tourId: {}", id);
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    @PostMapping("")
    public ResponseEntity<Tour> createTour(@RequestBody @Validated TourDTO payload) {
        var newTour = tourService.createTour(payload);
        var location = String.format("http://localhost/api/v1/tours/%d", newTour.id());
        return ResponseEntity.created(URI.create(location)).body(newTour);
    }

//    @PutMapping("/{id}")
//    public Tour updateTour(@PathVariable int id, @RequestBody Tour tour) {
//        var updatedTour = new Tour(id, tour.title(), tour.maxPeople());
//        tourInMemDB.put(id, updatedTour);
//        logger.info("Updated tour: {}", tourInMemDB.get(id));
//        return tourInMemDB.get(id);
//    }
//
//    @DeleteMapping("/{id}")
//    public String deleteTour(@PathVariable int id) {
//        if (!tourInMemDB.containsKey(id)) {
//            logger.error("Delete -> tourId: {} not found", id);
//            return "Failed";
//        }
//        tourInMemDB.remove(id);
//        logger.info("Delete tourId: {} success", id);
//        return "Success to delete " + id;
//    }
}

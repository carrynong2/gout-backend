package dev.carrynong.goutbackend.tourcompany.controller;

import dev.carrynong.goutbackend.tourcompany.dto.RegisterTourCompanyDTO;
import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.tourcompany.service.TourCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tour-companies")
public class TourCompanyController {
    private final Logger logger = LoggerFactory.getLogger(TourCompanyController.class);
    private final TourCompanyService tourCompanyService;

    public TourCompanyController(TourCompanyService tourCompanyService) {
        this.tourCompanyService = tourCompanyService;
    }

    @PostMapping
    public ResponseEntity<TourCompany> registerTourCompany(@RequestBody @Validated RegisterTourCompanyDTO tourCompanyDTO) {
        var tourCompany = tourCompanyService.registerTourCompany(tourCompanyDTO);
        return ResponseEntity.ok(tourCompany);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<TourCompany> approveTourCompany(@PathVariable Integer id) {
        var approvedTourCompany = tourCompanyService.approvedTourCompany(id);
        logger.info("[approveCompany] company id: {} is approved", id);
        return ResponseEntity.ok(approvedTourCompany);
    }

}

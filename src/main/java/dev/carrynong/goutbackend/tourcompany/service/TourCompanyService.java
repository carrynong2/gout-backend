package dev.carrynong.goutbackend.tourcompany.service;

import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.tourcompany.dto.RegisterTourCompanyDTO;

public interface TourCompanyService {
    TourCompany registerTourCompany(RegisterTourCompanyDTO tourCompanyDTO);
    TourCompany approvedTourCompany(Integer id);
}

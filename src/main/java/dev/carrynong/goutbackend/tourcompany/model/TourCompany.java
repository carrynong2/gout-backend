package dev.carrynong.goutbackend.tourcompany.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "tour_company")
public record TourCompany(@Id Integer id, String name, String status) {
}

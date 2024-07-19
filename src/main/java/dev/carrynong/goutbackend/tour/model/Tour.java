package dev.carrynong.goutbackend.tour.model;

import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table(name = "tour")
public record Tour(@Id Integer id,
                   AggregateReference<TourCompany, Integer> tourCompanyId,
                   String title,
                   String description,
                   String location,
                   int numberOfPeople,
                   Instant activityDate,
                   String status) {
}

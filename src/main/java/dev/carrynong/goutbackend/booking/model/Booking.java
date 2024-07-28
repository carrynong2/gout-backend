package dev.carrynong.goutbackend.booking.model;


import dev.carrynong.goutbackend.tour.model.Tour;
import dev.carrynong.goutbackend.user.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table(name = "booking")
public record Booking(
        @Id Integer id,
        AggregateReference<User, Integer> userId,
        AggregateReference<Tour, Integer> tourId,
        String state,
        Instant bookingDate,
        Instant lastUpdated,
        String idempotentKey) {
}
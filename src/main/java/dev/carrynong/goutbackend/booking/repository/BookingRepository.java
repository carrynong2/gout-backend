package dev.carrynong.goutbackend.booking.repository;

import dev.carrynong.goutbackend.booking.model.Booking;
import dev.carrynong.goutbackend.tour.model.Tour;
import dev.carrynong.goutbackend.user.model.User;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BookingRepository extends CrudRepository<Booking, Integer> {
    Optional<Booking> findOneByIdempotentKey(String idempotentKey);
    Optional<Booking> findOneByUserIdAndTourId(
            AggregateReference<User, Integer> userId,
            AggregateReference<Tour, Integer> tourId);
}

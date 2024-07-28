package dev.carrynong.goutbackend.booking.service;

import dev.carrynong.goutbackend.booking.dto.BookingInfoDto;
import dev.carrynong.goutbackend.booking.dto.CancelBookingDto;
import dev.carrynong.goutbackend.booking.dto.RequestBookingDto;
import org.springframework.security.core.Authentication;

public interface BookingService {
    BookingInfoDto bookTour(Authentication authentication, RequestBookingDto body);
    BookingInfoDto cancelTour(Authentication authentication, CancelBookingDto body);
}

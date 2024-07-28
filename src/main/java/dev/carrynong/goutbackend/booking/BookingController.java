package dev.carrynong.goutbackend.booking;

import dev.carrynong.goutbackend.booking.dto.BookingInfoDto;
import dev.carrynong.goutbackend.booking.dto.CancelBookingDto;
import dev.carrynong.goutbackend.booking.dto.RequestBookingDto;
import dev.carrynong.goutbackend.booking.service.BookingService;
import dev.carrynong.goutbackend.tour.repository.TourCountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {
    private final TourCountRepository tourCountRepository;
    private final BookingService bookingService;

    public BookingController(TourCountRepository tourCountRepository, BookingService bookingService) {
        this.tourCountRepository = tourCountRepository;
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingInfoDto bookTour(
            @RequestHeader("idempotent-key") String idempotentKey,
            @RequestBody @Validated RequestBookingDto body,
            Authentication authentication) {
        RequestBookingDto updatedBody = new RequestBookingDto(idempotentKey, body.userId(), body.tourId());
        return bookingService.bookTour(authentication, updatedBody);
    }

    @PostMapping("/cancel")
    public BookingInfoDto cancelTour(
            @RequestHeader("idempotent-key") String idempotentKey,
            @RequestBody @Validated CancelBookingDto body,
            Authentication authentication) {
        CancelBookingDto updatedBody = new CancelBookingDto(idempotentKey, body.bookingId(), body.userId(), body.tourId());
        return bookingService.cancelTour(authentication, updatedBody);
    }

}
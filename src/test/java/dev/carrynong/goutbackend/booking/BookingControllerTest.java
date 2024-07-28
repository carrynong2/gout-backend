package dev.carrynong.goutbackend.booking;

import com.nimbusds.jose.Payload;
import dev.carrynong.goutbackend.booking.dto.BookingInfoDto;
import dev.carrynong.goutbackend.booking.dto.CancelBookingDto;
import dev.carrynong.goutbackend.booking.dto.RequestBookingDto;
import dev.carrynong.goutbackend.booking.service.BookingService;
import dev.carrynong.goutbackend.common.enumeration.BookingStatusEnum;
import dev.carrynong.goutbackend.tour.model.TourCount;
import dev.carrynong.goutbackend.tour.repository.TourCountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BookingControllerTest {

    @Mock
    private TourCountRepository tourCountRepository;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @Test
    void testBookTour() {
        // Given
        String idempotentKey = "uniqueKey";
        RequestBookingDto requestDto = new RequestBookingDto(idempotentKey, 1, 1);

        BookingInfoDto bookingInfoDto = new BookingInfoDto(
                1,  // bookingId
                1,  // userId
                1,  // tourId
                BookingStatusEnum.COMPLETED.name(),  // state
                12345  // qrReference
        );

        // Mock Authentication object
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(1); // Mock principal as userId for simplicity

        // Mock BookingService
        BookingService bookingService = Mockito.mock(BookingService.class);
        when(bookingService.bookTour(any(Authentication.class), any(RequestBookingDto.class)))
                .thenReturn(bookingInfoDto);

        // Mock BookingController
        BookingController bookingController = new BookingController(null, bookingService); // You can mock TourCountRepository if needed

        // When
        BookingInfoDto result = bookingController.bookTour(idempotentKey, requestDto, authentication);

        // Then
        assertEquals(bookingInfoDto, result);
    }

    @Test
    void testCancelTour() {
        // Given
        String idempotentKey = "uniqueKey";
        CancelBookingDto cancelDto = new CancelBookingDto(idempotentKey, 1, 1, 1);

        BookingInfoDto bookingInfoDto = new BookingInfoDto(
                1,  // bookingId
                1,  // userId
                1,  // tourId
                BookingStatusEnum.CANCELED.name(),  // state
                67890  // qrReference
        );

        // Mock Authentication object
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(1); // Mock principal as userId for simplicity

        // Mock BookingService
        BookingService bookingService = Mockito.mock(BookingService.class);
        when(bookingService.cancelTour(any(Authentication.class), any(CancelBookingDto.class)))
                .thenReturn(bookingInfoDto);

        // Mock BookingController
        BookingController bookingController = new BookingController(null, bookingService); // You can mock TourCountRepository if needed

        // When
        BookingInfoDto result = bookingController.cancelTour(idempotentKey, cancelDto, authentication);

        // Then
        assertEquals(bookingInfoDto, result);
    }

}
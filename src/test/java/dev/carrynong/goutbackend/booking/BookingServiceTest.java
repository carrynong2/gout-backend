package dev.carrynong.goutbackend.booking;

import dev.carrynong.goutbackend.booking.dto.BookingInfoDto;
import dev.carrynong.goutbackend.booking.dto.CancelBookingDto;
import dev.carrynong.goutbackend.booking.dto.RequestBookingDto;
import dev.carrynong.goutbackend.booking.model.Booking;
import dev.carrynong.goutbackend.booking.repository.BookingRepository;
import dev.carrynong.goutbackend.booking.service.BookingServiceImpl;
import dev.carrynong.goutbackend.common.enumeration.BookingStatusEnum;
import dev.carrynong.goutbackend.common.exception.BookingExistsException;
import dev.carrynong.goutbackend.common.exception.UserIdMismatchException;
import dev.carrynong.goutbackend.qrcode.QrCodeReference;
import dev.carrynong.goutbackend.qrcode.QrCodeService;
import dev.carrynong.goutbackend.payment.PaymentService;
import dev.carrynong.goutbackend.tour.service.TourCountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookingServiceTest {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private TourCountService tourCountService;
    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private Authentication authentication;
    @Mock
    private Jwt jwt;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenBookTourAndBookingDoesNotExist_thenCreateBookingAndReturnBookingInfoDto() {
        // Given
        RequestBookingDto request = new RequestBookingDto("uniqueKey", 1, 1);
        Booking newBooking = new Booking(
                null,
                AggregateReference.to(1),
                AggregateReference.to(1),
                BookingStatusEnum.PENDING.name(),
                Instant.now(),
                Instant.now(),
                "uniqueKey");
        Booking savedBooking = new Booking(
                1,
                AggregateReference.to(1),
                AggregateReference.to(1),
                BookingStatusEnum.PENDING.name(),
                Instant.now(),
                Instant.now(),
                "uniqueKey");
        QrCodeReference qrCode = new QrCodeReference(1, 1, "content", "ACTIVATED");

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("1");
        when(bookingRepository.findOneByUserIdAndTourId(AggregateReference.to(1), AggregateReference.to(1)))
                .thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(qrCodeService.generateQrForBooking(1)).thenReturn(qrCode);

        // When
        BookingInfoDto result = bookingService.bookTour(authentication, request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.bookingId());
        assertEquals(1, result.userId());
        assertEquals(1, result.tourId());
        assertEquals(BookingStatusEnum.PENDING.name(), result.state());
        assertEquals(1, result.qrReference());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(qrCodeService, times(1)).generateQrForBooking(1);
    }

    @Test
    void whenBookTourAndBookingExists_thenReturnExistingBooking() {
        // Given
        int userId = 1;
        int tourId = 1;
        RequestBookingDto request = new RequestBookingDto("uniqueKey", userId, tourId);
        Booking existingBooking = new Booking(
                1,
                AggregateReference.to(userId),
                AggregateReference.to(tourId),
                BookingStatusEnum.COMPLETED.name(),
                Instant.now(),
                Instant.now(),
                "uniqueKey");

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn(String.valueOf(userId));
        when(bookingRepository.findOneByUserIdAndTourId(
                AggregateReference.to(userId),
                AggregateReference.to(tourId))
        ).thenReturn(Optional.of(existingBooking));

        // When
        BookingExistsException thrown = assertThrows(BookingExistsException.class, () -> {
            bookingService.bookTour(authentication, request);
        });

        // Then
        String expectedMessage = String.format("UserId: %d already booked TourId: %d", userId, tourId);
        assertEquals(expectedMessage, thrown.getMessage());
    }
    @Test
    void whenBookTourWithMismatchedUserId_thenThrowUserIdMismatchException() {
        // Given
        RequestBookingDto request = new RequestBookingDto("uniqueKey", 1, 1);

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("2");

        // When & Then
        UserIdMismatchException thrown = assertThrows(UserIdMismatchException.class, () -> {
            bookingService.bookTour(authentication, request);
        });

        assertEquals("User id mismatch between credential and payload", thrown.getMessage());
    }

    @Test
    void whenCancelTour_thenCancelBookingAndReturnBookingInfoDto() {
        // Given
        CancelBookingDto request = new CancelBookingDto("uniqueKey", 1, 1, 1);
        Booking existingBooking = new Booking(
                1,
                AggregateReference.to(1),
                AggregateReference.to(1),
                BookingStatusEnum.PENDING.name(),
                Instant.now(),
                Instant.now(),
                "uniqueKey");

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("1");
        when(bookingRepository.findById(1)).thenReturn(Optional.of(existingBooking));

        // When
        BookingInfoDto result = bookingService.cancelTour(authentication, request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.bookingId());
        assertEquals(1, result.userId());
        assertEquals(1, result.tourId());
        assertEquals(BookingStatusEnum.CANCELED.name(), result.state());
        assertNull(result.qrReference());
        verify(tourCountService, times(1)).decrementTourCount(1);
        verify(paymentService, times(1)).refundOnBooking("uniqueKey", 1);
        verify(bookingRepository, times(1)).deleteById(1);
    }

    @Test
    void whenCancelTourWithUserIdMismatch_thenThrowUserIdMismatchException() {
        // Given
        CancelBookingDto request = new CancelBookingDto("uniqueKey", 1, 1, 1);

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("2");

        // When & Then
        UserIdMismatchException thrown = assertThrows(UserIdMismatchException.class, () -> {
            bookingService.cancelTour(authentication, request);
        });

        assertEquals("User id mismatch between credential and payload", thrown.getMessage());
    }
}

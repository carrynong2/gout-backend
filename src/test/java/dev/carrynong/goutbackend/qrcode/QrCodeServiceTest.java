package dev.carrynong.goutbackend.qrcode;

import dev.carrynong.goutbackend.common.enumeration.QrCodeStatus;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.common.helper.QrCodeHelper;
import dev.carrynong.goutbackend.qrcode.QrCodeReference;
import dev.carrynong.goutbackend.qrcode.QrCodeReferenceRepository;
import dev.carrynong.goutbackend.qrcode.QrCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QrCodeServiceTest {

    @Mock
    private QrCodeReferenceRepository qrCodeReferenceRepository;

    @InjectMocks
    private QrCodeService qrCodeService;

    @Test
    void whenGenerateQrByIdThenReturnQrCodeImage() throws Exception {
        var id = 1;
        var content = "QRCodeContent";
        var qrCodeReference = new QrCodeReference(id, 123, content, QrCodeStatus.ACTIVATED.name());

        when(qrCodeReferenceRepository.findById(id)).thenReturn(Optional.of(qrCodeReference));

        var qrCodeImage = qrCodeService.generateQrById(id);

        assertNotNull(qrCodeImage);
        assertTrue(qrCodeImage instanceof BufferedImage);
        verify(qrCodeReferenceRepository, times(1)).findById(id);
    }

    @Test
    void whenGenerateQrByIdThenThrowEntityNotFoundException() {
        var id = 1;

        when(qrCodeReferenceRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> qrCodeService.generateQrById(id));
        verify(qrCodeReferenceRepository, times(1)).findById(id);
    }

    @Test
    void whenGenerateQrForBookingThenReturnQrCodeReference() {
        var bookingId = 1;
        var content = "QRCodeContent";
        var qrCodeReference = new QrCodeReference(null, bookingId, content, QrCodeStatus.ACTIVATED.name());

        when(qrCodeReferenceRepository.findOneByBookingId(bookingId)).thenReturn(Optional.empty());
        when(qrCodeReferenceRepository.save(any(QrCodeReference.class))).thenReturn(qrCodeReference);

        var result = qrCodeService.generateQrForBooking(bookingId);

        assertNotNull(result);
        assertEquals(bookingId, result.bookingId());
        assertEquals(content, result.content());
        assertEquals(QrCodeStatus.ACTIVATED.name(), result.status());
        verify(qrCodeReferenceRepository, times(1)).findOneByBookingId(bookingId);
        verify(qrCodeReferenceRepository, times(1)).save(any(QrCodeReference.class));
    }

    @Test
    void whenUpdateQrStatusThenReturnUpdatedQrCodeReference() {
        var bookingId = 1;
        var content = "QRCodeContent";
        var qrCodeReference = new QrCodeReference(1, bookingId, content, QrCodeStatus.ACTIVATED.name());

        when(qrCodeReferenceRepository.findOneByBookingId(bookingId)).thenReturn(Optional.of(qrCodeReference));
        when(qrCodeReferenceRepository.save(any(QrCodeReference.class))).thenReturn(qrCodeReference);

        var result = qrCodeService.updateQrStatus(bookingId, QrCodeStatus.ACTIVATED);

        assertNotNull(result);
        assertEquals(bookingId, result.bookingId());
        assertEquals(content, result.content());
        assertEquals(QrCodeStatus.ACTIVATED.name(), result.status());
        verify(qrCodeReferenceRepository, times(1)).findOneByBookingId(bookingId);
        verify(qrCodeReferenceRepository, times(1)).save(any(QrCodeReference.class));
    }

    @Test
    void whenUpdateQrStatusThenThrowEntityNotFoundException() {
        var bookingId = 1;

        when(qrCodeReferenceRepository.findOneByBookingId(bookingId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> qrCodeService.updateQrStatus(bookingId, QrCodeStatus.EXPIRED));
        verify(qrCodeReferenceRepository, times(1)).findOneByBookingId(bookingId);
    }
}
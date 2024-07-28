package dev.carrynong.goutbackend.qrcode;

import dev.carrynong.goutbackend.common.Constants;
import dev.carrynong.goutbackend.common.enumeration.QrCodeStatus;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.common.helper.QrCodeHelper;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class QrCodeService {
    private final QrCodeReferenceRepository qrCodeReferenceRepository;

    public QrCodeService(QrCodeReferenceRepository qrCodeReferenceRepository) {
        this.qrCodeReferenceRepository = qrCodeReferenceRepository;
    }

    public BufferedImage generateQrById(int id) throws Exception {
        var optionalQrCodeRef = qrCodeReferenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("QR Id: %d not found", id)));
        return QrCodeHelper.generateQRCodeImage(optionalQrCodeRef.content());
    }

    public QrCodeReference generateQrForBooking(int bookingId) {
        var optionalQrCodeRef = qrCodeReferenceRepository.findOneByBookingId(bookingId);
        if (optionalQrCodeRef.isPresent()) {
            return optionalQrCodeRef.get();
        }
        var paymentApiPath = String.format("%s/%d", Constants.PAYMENT_PATH, bookingId);
        var qrCodeEntity = new QrCodeReference(null, bookingId, paymentApiPath, QrCodeStatus.ACTIVATED.name());
        return qrCodeReferenceRepository.save(qrCodeEntity);
    }

    public QrCodeReference updateQrStatus(int bookingId, QrCodeStatus status) {
        var optinalQrCodeRef = qrCodeReferenceRepository.findOneByBookingId(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("QR for bookingId: %d not found", bookingId)));
        var qrCodeEntity = new QrCodeReference(
                optinalQrCodeRef.id(),
                optinalQrCodeRef.bookingId(),
                optinalQrCodeRef.content(),
                status.name());
        return qrCodeReferenceRepository.save(qrCodeEntity);
    }


}

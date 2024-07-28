package dev.carrynong.goutbackend.qrcode;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface QrCodeReferenceRepository extends CrudRepository<QrCodeReference, Integer> {
    Optional<QrCodeReference> findOneByBookingId(Integer bookingId);
}

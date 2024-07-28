package dev.carrynong.goutbackend.qrcode;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "qr_code_reference")
public record QrCodeReference(
        @Id Integer id,
        Integer bookingId,
        String content,
        String status
) {
}

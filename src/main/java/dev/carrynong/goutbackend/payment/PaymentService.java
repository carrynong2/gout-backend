package dev.carrynong.goutbackend.payment;

import dev.carrynong.goutbackend.booking.dto.BookingInfoDto;

import java.awt.image.BufferedImage;

public interface PaymentService {
    BufferedImage generatePaymentQr(int id) throws Exception;
    BookingInfoDto paymentOnBooking(String idempotentKey, int bookingId);
    void refundOnBooking(String idempotentKey, int bookingId);
}

package dev.carrynong.goutbackend.payment;

import dev.carrynong.goutbackend.booking.dto.BookingInfoDto;
import dev.carrynong.goutbackend.payment.PaymentService;
import dev.carrynong.goutbackend.payment.PaymentController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.awt.image.BufferedImage;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {
    @Mock
    private PaymentService paymentService;
    @InjectMocks
    private PaymentController paymentController;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    void testGetQrCodeById() throws Exception {
        int qrId = 1;
        BufferedImage mockImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        when(paymentService.generatePaymentQr(qrId)).thenReturn(mockImage);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payment/qr/{id}", qrId)
                        .accept(MediaType.IMAGE_PNG))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(result -> {
                    // Add additional checks here if needed
                });

        verify(paymentService, times(1)).generatePaymentQr(qrId);
    }

    @Test
    void testPayment() throws Exception {
        int bookingId = 1;
        String idempotentKey = "uniqueKey";
        BookingInfoDto bookingInfoDto = new BookingInfoDto(bookingId, 1, 1, "COMPLETED", 1);

        when(paymentService.paymentOnBooking(idempotentKey, bookingId)).thenReturn(bookingInfoDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payment/{bookingId}", bookingId)
                        .header("idempotent-key", idempotentKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.tourId").value(1))
                .andExpect(jsonPath("$.state").value("COMPLETED"))
                .andExpect(jsonPath("$.qrReference").value(1));

        verify(paymentService, times(1)).paymentOnBooking(idempotentKey, bookingId);
    }
}
package dev.carrynong.goutbackend.tourcompany;

import dev.carrynong.goutbackend.common.enumeration.TourCompanyStatus;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.tourcompany.dto.RegisterTourCompanyDTO;
import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.tourcompany.model.TourCompanyLogin;
import dev.carrynong.goutbackend.wallet.model.TourCompanyWallet;
import dev.carrynong.goutbackend.tourcompany.repositoriy.TourCompanyLoginRepository;
import dev.carrynong.goutbackend.tourcompany.repositoriy.TourCompanyRepository;
import dev.carrynong.goutbackend.wallet.repository.TourCompanyWalletRepository;
import dev.carrynong.goutbackend.tourcompany.service.TourCompanyServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TourCompanyServiceTest {
    @InjectMocks
    private TourCompanyServiceImpl tourCompanyService;
    @Mock
    private TourCompanyRepository tourCompanyRepository;
    @Mock
    private TourCompanyLoginRepository tourCompanyLoginRepository;
    @Mock
    private TourCompanyWalletRepository tourCompanyWalletRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void whenRegisterTourThenSuccess() {
        var mockTourCompany = new TourCompany(1, "Nong Tour", TourCompanyStatus.WAITING.name());
        when(tourCompanyRepository.save(ArgumentMatchers.any(TourCompany.class)))
                .thenReturn(mockTourCompany);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encryptedValue");
        var companyCredential = new TourCompanyLogin(1, AggregateReference.to(1), "nong", "encryptedValue");
        when(tourCompanyLoginRepository.save(ArgumentMatchers.any(TourCompanyLogin.class)))
                .thenReturn(companyCredential);

        var payload = new RegisterTourCompanyDTO(null, "Nong Tour",
                "nong", "123456789", null);
        var actual = tourCompanyService.registerTourCompany(payload);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.id().intValue());
        Assertions.assertEquals("Nong Tour", actual.name());
        Assertions.assertEquals(TourCompanyStatus.WAITING.name(), actual.status());
    }

    @Test
    void whenApproveTourThenSuccess() {
        var mockTourCompany = new TourCompany(1, "Nong Tour", TourCompanyStatus.WAITING.name());
        when(tourCompanyRepository.findById(anyInt()))
                .thenReturn(Optional.of(mockTourCompany));

        var updatedTourCompany = new TourCompany(mockTourCompany.id(), mockTourCompany.name(),
                TourCompanyStatus.APPROVED.name());
        when(tourCompanyRepository.save(any(TourCompany.class)))
                .thenReturn(updatedTourCompany);

        var wallet = new TourCompanyWallet(null, AggregateReference.to(1), Instant.now(), new BigDecimal("0.00"));
        when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class)))
                .thenReturn(wallet);
        tourCompanyWalletRepository.save(wallet);

        var actual = tourCompanyService.approvedTourCompany(1);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.id().intValue());
        Assertions.assertEquals("Nong Tour", actual.name());
        Assertions.assertEquals(TourCompanyStatus.APPROVED.name(), actual.status());
    }

    @Test
    void whenApproveTourButTourCompanyNotFoundThenError() {
        when(tourCompanyRepository.findById(anyInt()))
                .thenThrow(new EntityNotFoundException(String.format("Tour Company Id: %s not found", 1)));
        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            tourCompanyService.approvedTourCompany(1);
        });
    }
}

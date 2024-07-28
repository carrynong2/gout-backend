package dev.carrynong.goutbackend.tour;

import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.tour.model.TourCount;
import dev.carrynong.goutbackend.tour.repository.TourCountRepository;
import dev.carrynong.goutbackend.tour.service.TourCountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TourCountServiceTest {
    @InjectMocks
    private TourCountServiceImpl tourCountService;
    @Mock
    private TourCountRepository tourCountRepository;

    @BeforeEach
    void setup() {
        // Any setup needed before each test
    }

    @Test
    void testIncrementTourCount() {
        // Given
        int tourId = 1;
        TourCount existingTourCount = new TourCount(1, AggregateReference.to(tourId), 5);
        TourCount updatedTourCount = new TourCount(1, AggregateReference.to(tourId), 6);

        when(tourCountRepository.findOneByTourId(AggregateReference.to(tourId)))
                .thenReturn(Optional.of(existingTourCount));

        when(tourCountRepository.save(any(TourCount.class)))
                .thenReturn(updatedTourCount);

        // When
        tourCountService.incrementTourCount(tourId);

        // Then
        verify(tourCountRepository, times(1)).findOneByTourId(AggregateReference.to(tourId));
        verify(tourCountRepository, times(1)).save(updatedTourCount);
    }

    @Test
    void testIncrementTourCountWhenTourCountNotFound() {
        // Given
        int tourId = 1;
        when(tourCountRepository.findOneByTourId(AggregateReference.to(tourId)))
                .thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            tourCountService.incrementTourCount(tourId);
        });

        assertEquals(String.format("TourCount for tourId: %d not found", tourId), exception.getMessage());
        verify(tourCountRepository, times(1)).findOneByTourId(AggregateReference.to(tourId));
        verify(tourCountRepository, never()).save(any(TourCount.class));
    }

    @Test
    void testDecrementTourCount() {
        // Given
        int tourId = 1;
        TourCount existingTourCount = new TourCount(1, AggregateReference.to(tourId), 5);
        TourCount updatedTourCount = new TourCount(1, AggregateReference.to(tourId), 4);

        when(tourCountRepository.findOneByTourId(AggregateReference.to(tourId)))
                .thenReturn(Optional.of(existingTourCount));

        when(tourCountRepository.save(any(TourCount.class)))
                .thenReturn(updatedTourCount);

        // When
        tourCountService.decrementTourCount(tourId);

        // Then
        verify(tourCountRepository, times(1)).findOneByTourId(AggregateReference.to(tourId));
        verify(tourCountRepository, times(1)).save(updatedTourCount);
    }

    @Test
    void testDecrementTourCountWhenTourCountNotFound() {
        // Given
        int tourId = 1;
        when(tourCountRepository.findOneByTourId(AggregateReference.to(tourId)))
                .thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            tourCountService.decrementTourCount(tourId);
        });

        assertEquals(String.format("TourCount for tourId: %d not found", tourId), exception.getMessage());
        verify(tourCountRepository, times(1)).findOneByTourId(AggregateReference.to(tourId));
        verify(tourCountRepository, never()).save(any(TourCount.class));
    }
}
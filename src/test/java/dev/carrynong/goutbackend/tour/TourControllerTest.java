package dev.carrynong.goutbackend.tour;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import dev.carrynong.goutbackend.common.enumeration.TourStatus;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.tour.controller.TourController;
import dev.carrynong.goutbackend.tour.dto.TourDTO;
import dev.carrynong.goutbackend.tour.model.Tour;
import dev.carrynong.goutbackend.tour.service.TourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebMvcTest(TourController.class)
public class TourControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TourService tourService;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void whenGetPageTourThenSuccessful() throws Exception {
        var tour = new Tour(
                1,
                AggregateReference.to(1),
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                Instant.now().plus(Duration.ofDays(5)),
                TourStatus.PENDING.name());
        List<Tour> tours = List.of(tour);
        Page<Tour> pageTours = new PageImpl<>(tours);
        when(tourService.getPageTour(any(Pageable.class)))
                .thenReturn(pageTours);

        mockMvc.perform(MockMvcRequestBuilders.get(
                                String.format("/api/v1/tours?page=0&size=2&sortField=id&sortDirection=asc", 1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray());
    }

    @Test
    void whenGetPageTourButForgotRequireQueryString() throws Exception {
        var tour = new Tour(
                1,
                AggregateReference.to(1),
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                Instant.now().plus(Duration.ofDays(5)),
                TourStatus.PENDING.name());
        List<Tour> tours = List.of(tour);
        Page<Tour> pageTours = new PageImpl<>(tours);
        when(tourService.getPageTour(any(Pageable.class)))
                .thenReturn(pageTours);

        mockMvc.perform(MockMvcRequestBuilders.get(
                                String.format("/api/v1/tours?sortField=id&sortDirection=asc", 1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void whenCreateTourThenSuccessful() throws Exception {
        var activityDate = Instant.now().plus(Duration.ofDays(5));
        var payload = new TourDTO(
                1,
                "Camping",
                "Camping 3 day 2 night",
                "Forest",
                10,
                activityDate,
                TourStatus.PENDING.name()
        );
        var tour = new Tour(1, AggregateReference.to(1), "Camping",
                "Camping 3 day 2 night", "Forest", 10,
                activityDate, TourStatus.PENDING.name());
        when(tourService.createTour(any(TourDTO.class)))
                .thenReturn(tour);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/tours")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));
    }

    @Test
    void whenCreateTourButMissingSomeFieldsThen400() throws Exception {
        var activityDate = Instant.now().plus(Duration.ofDays(5));
        var payload = new TourDTO(
                1,
                null,
                null,
                "Forest",
                10,
                activityDate,
                TourStatus.PENDING.name()
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/tours")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void whenGetTourByIdThenSuccessful() throws Exception {
        var tour = new Tour(
                1,
                AggregateReference.to(1),
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                Instant.now().plus(Duration.ofDays(5)),
                TourStatus.PENDING.name());
        when(tourService.getTourById((anyInt())))
                .thenReturn(tour);

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/v1/tours/%d", 1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));
    }

    @Test
    void whenGetTourByIdButNotFoundThenReturn404() throws Exception {
        when(tourService.getTourById(anyInt()))
                .thenThrow(new EntityNotFoundException());

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/v1/tours/%d", 1)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void whenGetTourByIdButServerErrorThenReturn500() throws Exception {
        when(tourService.getTourById(anyInt()))
                .thenThrow(new InternalServerErrorException("Mock error"));

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/v1/tours/%d", 1)))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }
}

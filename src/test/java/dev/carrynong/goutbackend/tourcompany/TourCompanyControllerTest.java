package dev.carrynong.goutbackend.tourcompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.carrynong.goutbackend.common.enumeration.TourCompanyStatus;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.tourcompany.controller.TourCompanyController;
import dev.carrynong.goutbackend.tourcompany.dto.RegisterTourCompanyDTO;
import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.tourcompany.service.TourCompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebMvcTest(TourCompanyController.class)
public class TourCompanyControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TourCompanyService tourCompanyService;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void whenCreateTourCompanyThenSuccessful() throws Exception {
        var mockTourCompany = new TourCompany(1, "Nong Tour", TourCompanyStatus.WAITING.name());
        when(tourCompanyService.registerTourCompany(any(RegisterTourCompanyDTO.class)))
                .thenReturn(mockTourCompany);
        var payload = new RegisterTourCompanyDTO(null, "Nong Tour",
                "nong", "123456789", null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/tour-companies")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));
    }

    @Test
    void whenApproveTourCompanyThenSuccessful() throws Exception {
        var mockTourCompany = new TourCompany(1, "Nong Tour", TourCompanyStatus.APPROVED.name());
        when(tourCompanyService.approvedTourCompany(anyInt()))
                .thenReturn(mockTourCompany);

        mockMvc.perform(MockMvcRequestBuilders.post(String.format("/api/v1/tour-companies/%d/approve", 1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(TourCompanyStatus.APPROVED.name()));
    }

    @Test
    void whenApproveTourCompanyButNotFoundThenReturn404() throws Exception {
        when(tourCompanyService.approvedTourCompany(anyInt()))
                .thenThrow(new EntityNotFoundException());

        mockMvc.perform(MockMvcRequestBuilders.post(String.format("/api/v1/tour-companies/%d/approve", 1)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}

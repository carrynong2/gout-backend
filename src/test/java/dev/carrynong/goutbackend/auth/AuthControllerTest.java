package dev.carrynong.goutbackend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.carrynong.goutbackend.auth.controller.AuthController;
import dev.carrynong.goutbackend.auth.dto.LoginRequestDTO;
import dev.carrynong.goutbackend.auth.dto.LoginResponseDTO;
import dev.carrynong.goutbackend.auth.dto.LogoutDTO;
import dev.carrynong.goutbackend.auth.dto.RefreshTokenDTO;
import dev.carrynong.goutbackend.auth.service.AuthService;
import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void whenLoginSuccessfulThenReturnTokens() throws Exception {
        var loginRequest = new LoginRequestDTO("test@test.com", "123456789");
        var loginResponse = new LoginResponseDTO(1, "accessToken", "refreshToken", "bearer");
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tokenType").value("accessToken"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value("refreshToken"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value("bearer"));
    }

    @Test
    void whenRefreshTokenSuccessfulThenReturnNewTokens() throws Exception {
        var refreshTokenRequest = new RefreshTokenDTO(RoleEnum.CONSUMER.name(), 1, "refreshToken");
        var loginResponse = new LoginResponseDTO(1, "newAccessToken", "newRefreshToken", "bearer");
        when(authService.issueNewAccessToken(any(RefreshTokenDTO.class))).thenReturn(loginResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tokenType").value("newAccessToken"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value("newRefreshToken"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value("bearer"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenLogoutSuccessfulThenNoContent() throws Exception {
        // Mock authService.logout
        doNothing().when(authService).logout(any(LogoutDTO.class));

        // Perform the request
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}

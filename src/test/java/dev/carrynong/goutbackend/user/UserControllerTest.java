package dev.carrynong.goutbackend.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.carrynong.goutbackend.tour.model.Tour;
import dev.carrynong.goutbackend.user.controller.UserController;
import dev.carrynong.goutbackend.user.dto.UserCreationDTO;
import dev.carrynong.goutbackend.user.dto.UserInfoDTO;
import dev.carrynong.goutbackend.user.dto.UserUpdateDTO;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void whenGetPageUserThenSuccessful() throws Exception {
        var mockUser = new User(1, "Test", "Test", "0800125480");
        List<User> users = List.of(mockUser);
        Page<User> pageUsers = new PageImpl<>(users);
        when(userService.getUserByFirstName(anyString(), any(Pageable.class)))
                .thenReturn(pageUsers);

        mockMvc.perform(MockMvcRequestBuilders.get(
                                String.format("/api/v1/users?keyword=est&page=0&size=2&sortField=id&sortDirection=asc", 1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray());
    }


    @Test
    void whenGetUserByIdThenSuccessful() throws Exception {
        var mockUser = new UserInfoDTO(1, "Test", "Test", "0800125480");
        when(userService.getUserDTOById(anyInt()))
                .thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/v1/users/%d", 1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumber").value("0800125480"));
    }

    @Test
    void whenCreateUserThenSuccessful() throws Exception {
        var mockUser = new UserInfoDTO(1, "Test", "Test", "0800125480");
        when(userService.createUser(any(UserCreationDTO.class)))
                .thenReturn(mockUser);
        var body = new UserCreationDTO("Test", "Test",
                "0800125480", "test@test.com", "123456789");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumber").value("0800125480"));
    }

    @Test
    void whenUpdateUserThenSuccessful() throws Exception {
        var mockUser = new UserInfoDTO(1, "Test1", "Test1", "0800125480");
        when(userService.updateUser(anyInt(), any(UserUpdateDTO.class)))
                .thenReturn(mockUser);
        var body = new UserUpdateDTO("Test1", "Test1");

        mockMvc.perform(MockMvcRequestBuilders.patch(String.format("/api/v1/users/%d", 1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Test1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Test1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phoneNumber").value("0800125480"));
    }

    @Test
    void whenDeleteUserByIdThenSuccessful() throws Exception {
        when(userService.deleteUserById(anyInt()))
                .thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete(String.format("/api/v1/users/%d", 1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}

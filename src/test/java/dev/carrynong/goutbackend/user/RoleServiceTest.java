package dev.carrynong.goutbackend.user;

import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.user.model.Role;
import dev.carrynong.goutbackend.user.repository.RoleRepository;
import dev.carrynong.goutbackend.user.service.RoleService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
    @InjectMocks
    private RoleService roleService;
    @Mock
    private RoleRepository roleRepository;

    @Test
    void shouldReturnRoles() {
        var mockRoles = List.of(
                new Role(RoleEnum.COMPANY.getId(), RoleEnum.CONSUMER.name()),
                new Role(RoleEnum.ADMIN.getId(), RoleEnum.ADMIN.name()),
                new Role(RoleEnum.COMPANY.getId(), RoleEnum.COMPANY.name())
        );
        when(roleRepository.findAll())
                .thenReturn(mockRoles);

        var actual = roleService.getAllRole();
        List<Role> result = new ArrayList<>();
        actual.iterator().forEachRemaining(result::add);

        Assertions.assertEquals(3, result.size());
    }
}

package dev.carrynong.goutbackend.user.service;

import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.user.model.Role;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.user.model.UserRole;
import dev.carrynong.goutbackend.user.repository.RoleRepository;
import dev.carrynong.goutbackend.user.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final Logger logger = LoggerFactory.getLogger(RoleService.class);
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public RoleService(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public Iterable<Role> getAllRole() {
        var availableRoles = roleRepository.findAll();
        logger.info("availableRoles: {}", availableRoles);
        return availableRoles;
    }

    public UserRole bindingNewUser(int userId, RoleEnum roleEnum) {
        AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
        AggregateReference<Role, Integer> roleId = AggregateReference.to(roleEnum.getId());
        var prepareRole = new UserRole(null, userReference, roleId);
        return userRoleRepository.save(prepareRole);
    }
}

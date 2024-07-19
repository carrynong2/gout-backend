package dev.carrynong.goutbackend.user.service;

import dev.carrynong.goutbackend.auth.service.AuthService;
import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.common.exception.CredentialExistsException;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.user.dto.UserCreationDTO;
import dev.carrynong.goutbackend.user.dto.UserInfoDTO;
import dev.carrynong.goutbackend.user.dto.UserUpdateDTO;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.user.repository.UserRepository;
import dev.carrynong.goutbackend.user.repository.UserRoleRepository;
import dev.carrynong.goutbackend.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final AuthService authService;
    private final RoleService roleService;

    public UserServiceImpl(UserRepository userRepository, WalletService walletService, AuthService authService, UserRoleRepository userRoleRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.authService = authService;
        this.roleService = roleService;
    }

    @Override
    public UserInfoDTO getUserDTOById(Integer id) {
        var user = getUserById(id);
        return new UserInfoDTO(user.id(), user.firstName(), user.lastName(), user.phoneNumber());
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User Id: %s not found", id)));
    }

    // Create user + login credential + wallet
    @Override
    @Transactional
    public UserInfoDTO createUser(UserCreationDTO body) {
        var existsCred = authService.findCredentialByUsername(body.email());
        if (existsCred.isPresent()) {
            throw new CredentialExistsException(String.format("User: %s exists!", body.email()));
        }

        var prepareUser = new User(null, body.firstName(), body.lastName(), body.phoneNumber());
        var newUser = userRepository.save(prepareUser);

        var userRole = roleService.bindingNewUser(newUser.id(), RoleEnum.CONSUMER);

        authService.createConsumerCredential(newUser.id(), body.email(), body.password());
        walletService.createConsumerWallet(newUser.id());
        return new UserInfoDTO(newUser.id(), newUser.firstName(), newUser.lastName(), newUser.phoneNumber());
    }

    // Update user
    @Override
    @Transactional
    public UserInfoDTO updateUser(Integer id,UserUpdateDTO body) {
        var user = getUserById(id);
        var prepareUser = new User(user.id(), body.firstName(), body.lastName(), user.phoneNumber());
        var updatedUser = userRepository.save(prepareUser);
        return new UserInfoDTO(updatedUser.id(), updatedUser.firstName(), updatedUser.lastName(), updatedUser.phoneNumber());
    }

    // Delete user + credential & wallet removal
    @Override
    @Transactional
    public boolean deleteUserById(Integer id) {
        var user = getUserById(id);
        authService.deleteCredentialByUserId(user.id());
        logger.info("Delete credential for userId: {}", user.id());
        walletService.deleteConsumerWalletByUserId(user.id());
        logger.info("Delete wallet for userId: {}", user.id());
        userRepository.delete(user);
        logger.info("Delete userId: {}", user.id());
        return true;
    }

}

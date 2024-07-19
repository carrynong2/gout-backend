package dev.carrynong.goutbackend.auth.service;

import dev.carrynong.goutbackend.auth.dto.AuthenticateUser;
import dev.carrynong.goutbackend.auth.dto.LoginRequestDTO;
import dev.carrynong.goutbackend.auth.dto.LoginResponseDTO;
import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.auth.repository.UserLoginRepository;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Primary
public class AuthServiceImpl implements AuthService {
    private final UserLoginRepository userLoginRepository;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthServiceImpl(UserLoginRepository userLoginRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService) {
        this.userLoginRepository = userLoginRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @Override
    public Optional<UserLogin> findCredentialByUsername(String email) {
        return userLoginRepository.findByEmail(email);
    }

    @Override
    public Optional<UserLogin> findCredentialByUserId(int userId) {
        AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
        return userLoginRepository.findByUserId(userReference);
    }

    @Override
    public UserLogin createConsumerCredential(int userId, String email, String password) {
        AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
        var encryptedPassword = passwordEncoder.encode(password);
        var userCredential = new UserLogin(null, userReference, email, encryptedPassword);
        var createdCredential = userLoginRepository.save(userCredential);
        logger.info("Create credential for user: {}", userId);
        return createdCredential;
    }

    @Override
    public void deleteCredentialByUserId(int userId) {
        var credential = findCredentialByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Credential for User Id: %s not found", userId)));
        userLoginRepository.delete(credential);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO body) {
        var authInfo = new UsernamePasswordAuthenticationToken(body.username(), body.password());
        var authentication = authenticationManager.authenticate(authInfo);
        var authenticatedUser = (AuthenticateUser) authentication.getPrincipal();
        var token = tokenService.generateToken(authentication);
        return new LoginResponseDTO(authenticatedUser.userId(), token);
    }

}

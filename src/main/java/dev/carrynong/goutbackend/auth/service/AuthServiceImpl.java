package dev.carrynong.goutbackend.auth.service;

import dev.carrynong.goutbackend.auth.dto.*;
import dev.carrynong.goutbackend.auth.model.RefreshToken;
import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.auth.repository.RefreshTokenRepository;
import dev.carrynong.goutbackend.auth.repository.UserLoginRepository;
import dev.carrynong.goutbackend.common.Constants;
import dev.carrynong.goutbackend.common.enumeration.RoleEnum;
import dev.carrynong.goutbackend.common.exception.EntityNotFoundException;
import dev.carrynong.goutbackend.common.exception.RefreshTokenExpiredException;
import dev.carrynong.goutbackend.tourcompany.model.TourCompany;
import dev.carrynong.goutbackend.tourcompany.repositoriy.TourCompanyLoginRepository;
import dev.carrynong.goutbackend.tourcompany.repositoriy.TourCompanyRepository;
import dev.carrynong.goutbackend.user.model.User;
import dev.carrynong.goutbackend.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Primary
public class AuthServiceImpl implements AuthService {
    private final UserLoginRepository userLoginRepository;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TourCompanyLoginRepository tourCompanyLoginRepository;
    private final TourCompanyRepository tourCompanyRepository;

    public AuthServiceImpl(UserLoginRepository userLoginRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, TourCompanyLoginRepository tourCompanyLoginRepository, TourCompanyRepository tourCompanyRepository) {
        this.userLoginRepository = userLoginRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tourCompanyLoginRepository = tourCompanyLoginRepository;
        this.tourCompanyRepository = tourCompanyRepository;
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
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO body) {
        var authInfo = new UsernamePasswordAuthenticationToken(body.username(), body.password());
        var authentication = authenticationManager.authenticate(authInfo);
        var authenticatedUser = (AuthenticateUser) authentication.getPrincipal();
        var now = Instant.now();
        var accessToken = tokenService.issueAccessToken(authentication, now);
        var refreshToken = tokenService.issueRefreshToken();

        logout(authentication);

        var prepareRefreshToken = new RefreshToken(
                null, refreshToken, now, authenticatedUser.role().name(),
                authenticatedUser.userId(), false
        );

        refreshTokenRepository.save(prepareRefreshToken);

        return new LoginResponseDTO(
                authenticatedUser.userId(),
                Constants.TOKEN_TYPE,
                accessToken,
                refreshToken
        );
    }

    @Override
    @Transactional
    public LoginResponseDTO issueNewAccessToken(RefreshTokenDTO body) {
        // Check refresh token is exists?
        var refreshTokenEntity = refreshTokenRepository.findByToken(body.refreshToken())
                .orElseThrow(() -> new EntityNotFoundException("This refresh token not found"));
        var resourceId = refreshTokenEntity.resourceId();
        // Expired? - DB -> IssuedDate + configured expire time
        if (tokenService.isRefreshTokenExpired(refreshTokenEntity)) {
            logout(new LogoutDTO(String.valueOf(resourceId), refreshTokenEntity.usage()));
            // Need re-login
            throw new RefreshTokenExpiredException("This refresh token is expired");
        }
        // Token almost expired => refresh token rotation
        String newAccessToken = switch (RoleEnum.valueOf(body.usage())) {
            case RoleEnum.COMPANY -> {
                AggregateReference<TourCompany, Integer> tourCompanyReference = AggregateReference.to(resourceId);
                var credential = tourCompanyLoginRepository.findOneByTourCompanyId(tourCompanyReference)
                        .orElseThrow(() -> new EntityNotFoundException(
                                String.format("Company Id: %d not found", resourceId)));
                yield tokenService.issueAccessToken(credential, Instant.now());
            }
            default -> {
                var user = userRepository.findById(resourceId)
                        .orElseThrow(() -> new EntityNotFoundException(
                                String.format("User Id: %d not found", resourceId)));
                var credential = findCredentialByUserId(user.id())
                        .orElseThrow(
                                () -> new EntityNotFoundException(
                                        String.format("Credential for user Id: %d not found",
                                                user.id())));
                yield tokenService.issueAccessToken(credential, Instant.now());
            }
        };
        var refreshToken = tokenService.rotateRefreshTokenIfNeed(refreshTokenEntity);
        // Check if refresh token change -> change old refresh token to expired
        if (!refreshToken.equals(refreshTokenEntity.token())) {
            var updatedRefreshTokenEntity = new RefreshToken(
                    refreshTokenEntity.id(),
                    refreshTokenEntity.token(),
                    refreshTokenEntity.issuedDate(),
                    refreshTokenEntity.usage(),
                    refreshTokenEntity.resourceId(),
                    true);
            refreshTokenRepository.save(updatedRefreshTokenEntity);
            var prepareRefreshTokenModel = new RefreshToken(
                    null,
                    refreshToken,
                    Instant.now(),
                    refreshTokenEntity.usage(),
                    refreshTokenEntity.resourceId(),
                    false);
            refreshTokenRepository.save(prepareRefreshTokenModel);
        }
        return new LoginResponseDTO(
                refreshTokenEntity.resourceId(),
                Constants.TOKEN_TYPE,
                newAccessToken,
                refreshToken);
    }

    @Override
    public void logout(Authentication authentication) {
        var authenticatedUser = (AuthenticateUser) authentication.getPrincipal();
        refreshTokenRepository.updateRefreshTokenByResource(
                authenticatedUser.role().name(),
                authenticatedUser.userId(),
                true);
    }

    @Override
    public void logout(LogoutDTO logoutDTO) {
        refreshTokenRepository.updateRefreshTokenByResource(
                logoutDTO.roles(),
                Integer.parseInt(logoutDTO.sub()),
                true);
    }

}

package dev.carrynong.goutbackend.auth.service;

import dev.carrynong.goutbackend.auth.dto.AuthenticateUser;
import dev.carrynong.goutbackend.auth.model.RefreshToken;
import dev.carrynong.goutbackend.auth.model.UserLogin;
import dev.carrynong.goutbackend.auth.repository.RefreshTokenRepository;
import dev.carrynong.goutbackend.tourcompany.model.TourCompanyLogin;
import dev.carrynong.goutbackend.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TokenService {
    private static final String ISSUER = "gout-backend";
    private static final String ROLES_CLAIM = "roles";
    private static final int TIME_FOR_ROTATE_SECOND = 530;
    private final JwtEncoder jwtEncoder;
    private final long accessTokenExpiredInSeconds;
    private final long refreshTokenExpiredInSeconds;
    private final CustomUserDetailService customUserDetailService;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${token.access-token-expired-in-seconds}")
                        long accessTokenExpiredInSeconds,
                        @Value("${token.refresh-token-expired-in-seconds}")
                        long refreshTokenExpiredInSeconds, CustomUserDetailService customUserDetailService, RefreshTokenRepository refreshTokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenExpiredInSeconds = accessTokenExpiredInSeconds;
        this.refreshTokenExpiredInSeconds = refreshTokenExpiredInSeconds;
        this.customUserDetailService = customUserDetailService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String issueAccessToken(Authentication auth, Instant issueDate) {
        return generateToken(auth, issueDate, accessTokenExpiredInSeconds);
    }

    public String issueAccessToken(UserLogin userLogin, Instant issueDate) {
        AuthenticateUser userDetails = (AuthenticateUser) customUserDetailService.loadUserByUsername(userLogin.email());
        return generateToken(userDetails, issueDate, accessTokenExpiredInSeconds);
    }

    public String issueAccessToken(TourCompanyLogin tourCompanyLogin, Instant issueDate) {
        AuthenticateUser userDetails = (AuthenticateUser) customUserDetailService.loadUserByUsername(tourCompanyLogin.username());
        return generateToken(userDetails, issueDate, accessTokenExpiredInSeconds);
    }

    public String issueRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public String generateToken(Authentication auth, Instant issueDate, long expiredInSeconds) {
        var authenticatedUser = (AuthenticateUser) auth.getPrincipal();
        return generateToken(authenticatedUser.userId(), auth.getAuthorities(), issueDate, expiredInSeconds);
    }

    private String generateToken(Integer userId, Collection<? extends GrantedAuthority> authorities, Instant issueDate, long expiredInSeconds) {
        Instant expire = issueDate.plusSeconds(expiredInSeconds);
        String scope = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(issueDate)
                .subject(String.valueOf(userId))
                .claim(ROLES_CLAIM, scope)
                .expiresAt(expire)
                .build();

        return encodeClaimToJwt(claims);
    }

    public String generateToken(AuthenticateUser auth, Instant issueDate, long expiredInSeconds) {
        return generateToken(auth.userId(), auth.getAuthorities(), issueDate, expiredInSeconds);
    }

    public String encodeClaimToJwt(JwtClaimsSet claims) {
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }


    public boolean isRefreshTokenExpired(RefreshToken refreshToken) {
        var issuedDate = refreshToken.issuedDate();
        var expireDate = issuedDate.plusSeconds(refreshTokenExpiredInSeconds);
        var now = Instant.now();
        return now.isAfter(expireDate);
    }

    public String rotateRefreshTokenIfNeed(RefreshToken refreshTokenEntity) {
        var issuedDate = refreshTokenEntity.issuedDate();
        var expireDate = issuedDate.plusSeconds(refreshTokenExpiredInSeconds);
        var thresholdToRotateDate = expireDate.minusSeconds(TIME_FOR_ROTATE_SECOND);
        var now = Instant.now();
        if (now.isAfter(thresholdToRotateDate)) {
            return issueRefreshToken();
        }
        return refreshTokenEntity.token();
    }

    public void cleanupRefreshTokenThatNotExpired() {
        var now = Instant.now();
        // Assume life of refresh token = 1 day
        // Token issued on 202426174716
        // Token expired on 202426184716
        // Cron start at   202426184716
        // If we want to check expired token from issuedDate -> minus seconds
        var thresholdDate = now.minusSeconds(refreshTokenExpiredInSeconds);
        refreshTokenRepository.updateRefreshTokenThatExpired(true, thresholdDate);
    }
}

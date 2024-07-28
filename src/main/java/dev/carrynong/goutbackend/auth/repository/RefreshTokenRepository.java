package dev.carrynong.goutbackend.auth.repository;

import dev.carrynong.goutbackend.auth.model.RefreshToken;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Integer> {
    @Modifying
    @Query("UPDATE refresh_token SET is_expired = :isExpired WHERE usage = :usage AND resource_id = :resourceId")
    Optional<RefreshToken> updateRefreshTokenByResource(String usage,
                                                        int resourceId,
                                                        boolean isExpired);
    @Modifying
    @Query("UPDATE refresh_token SET is_expired = :isExpired WHERE is_expired = false AND issued_date <= :thresholdDate")
    Optional<RefreshToken> updateRefreshTokenThatExpired(boolean isExpired, Instant thresholdDate);
    Optional<RefreshToken> findByToken(String token);
}

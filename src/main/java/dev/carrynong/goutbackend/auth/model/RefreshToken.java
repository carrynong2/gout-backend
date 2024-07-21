package dev.carrynong.goutbackend.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table(name = "refresh_token")
public record RefreshToken(
        @Id
        Integer id,
        String token,
        Instant issuedDate,
        String usage,
        Integer resourceId,
        Boolean isExpired) {
}

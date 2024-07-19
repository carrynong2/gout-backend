package dev.carrynong.goutbackend.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "user")
public record User(
        @Id Integer id,
        String firstName,
        String lastName,
        String phoneNumber
) {
}

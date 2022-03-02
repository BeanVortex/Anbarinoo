package ir.darkdeveloper.anbarinoo.dto;

import ir.darkdeveloper.anbarinoo.model.Auth.AuthProvider;

import java.time.LocalDateTime;

public record UserDto(Long id, String email, String userName, Boolean enabled,
                      String shopImage, String profileImage, String shopName,
                      String address, String description, LocalDateTime createdAt,
                      AuthProvider provider, LocalDateTime updatedAt) {
}

package ir.darkdeveloper.anbarinoo.dto;

import ir.darkdeveloper.anbarinoo.model.UserModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DebtOrDemandDto(Long id, String nameOf,
                              String payTo, Boolean isDebt,
                              Boolean isCheckedOut, BigDecimal amount,
                              Long chequeId, Long userId, String issuedAt,
                              String validTill, String createdAt,
                              String updatedAt) {
}

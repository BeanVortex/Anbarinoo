package ir.darkdeveloper.anbarinoo.dto;

import java.math.BigDecimal;

public record DebtOrDemandDto(Long id, String nameOf,
                              String payTo, Boolean isDebt,
                              Boolean isCheckedOut, BigDecimal amount,
                              Long chequeId, Long userId, String issuedAt,
                              String validTill, String createdAt,
                              String updatedAt) {
}

package ir.darkdeveloper.anbarinoo.dto;

import java.math.BigDecimal;

public record ChequeDto(Long id, String nameOf,
                        String payTo, BigDecimal amount,
                        Boolean isDebt, Boolean isCheckedOut,
                        Long userId, String issuedAt,
                        String validTill, String createdAt,
                        String updatedAt) {
}

package ir.darkdeveloper.anbarinoo.dto;

import java.math.BigDecimal;

public record BuyDto(Long id, BigDecimal count,
                     BigDecimal price, Integer tax,
                     Long productId, String productName,
                     String createdAt, String updatedAt) {
}

package ir.darkdeveloper.anbarinoo.dto;

import java.math.BigDecimal;

public record SellDto(Long id, BigDecimal count,
                      BigDecimal price, Integer tax,
                      Long productId, String createdAt,
                      String updatedAt) {
}

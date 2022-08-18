package ir.darkdeveloper.anbarinoo.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(Long id, String name, String description,
                         List<String> images, BigDecimal price, Integer tax,
                         Long categoryId, BigDecimal totalCount,
                         String createdAt, String updatedAt) {
}

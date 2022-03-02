package ir.darkdeveloper.anbarinoo.dto;

import ir.darkdeveloper.anbarinoo.model.CategoryModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDto(Long id, Boolean canUpdate, String name, String description,
                         List<String> images, BigDecimal price, Integer tax,
                         Long categoryId, BigDecimal totalCount,
                         String createdAt, String updatedAt) {
}

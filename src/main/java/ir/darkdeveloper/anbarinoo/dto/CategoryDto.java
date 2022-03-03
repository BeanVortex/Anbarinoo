package ir.darkdeveloper.anbarinoo.dto;

import java.util.List;

public record CategoryDto(Long id, String name, Long userId, Long parentId,
                          List<Long> children, List<Long> products) {
}

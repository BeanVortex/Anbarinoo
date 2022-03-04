package ir.darkdeveloper.anbarinoo.dto;

import java.util.List;

public record UserRoleDto(Long id, String name,
                          List<String> authorities,
                          List<Long> users) {
}

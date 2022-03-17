package ir.darkdeveloper.anbarinoo.dto.mapper;

import ir.darkdeveloper.anbarinoo.dto.UserRoleDto;
import ir.darkdeveloper.anbarinoo.model.Authority;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.model.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    @Mappings({
            @Mapping(target = "authorities", expression = "java(convertAuthoritiesToListOfString(role))"),
            @Mapping(target = "users", expression = "java(convertUsersToListOfId(role))"),
    })
    UserRoleDto userRoleToDto(UserRole role);


    default List<String> convertAuthoritiesToListOfString(UserRole role) {
        if (role.getAuthorities() != null)
            return role.getAuthorities().stream().map(Authority::getAuthority).toList();
        return Collections.emptyList();
    }

    default List<Long> convertUsersToListOfId(UserRole role) {
        if (role.getUsers() != null)
            return role.getUsers().stream().map(UserModel::getId).toList();
        return Collections.emptyList();
    }
}

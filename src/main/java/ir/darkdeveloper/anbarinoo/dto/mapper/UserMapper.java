package ir.darkdeveloper.anbarinoo.dto.mapper;

import ir.darkdeveloper.anbarinoo.config.StartupConfig;
import ir.darkdeveloper.anbarinoo.dto.UserDto;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mappings({
            @Mapping(target = "createdAt", dateFormat = StartupConfig.DATE_FORMAT),
            @Mapping(target = "updatedAt", dateFormat = StartupConfig.DATE_FORMAT)
    })
    UserDto userToDto(UserModel model);

    @Mappings({
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "passwordRepeat", ignore = true),
            @Mapping(target = "prevPassword", ignore = true),
            @Mapping(target = "shopFile", ignore = true),
            @Mapping(target = "profileFile", ignore = true),
            @Mapping(target = "roles", ignore = true),
            @Mapping(target = "debtOrDemand", ignore = true),
            @Mapping(target = "cheques", ignore = true),
            @Mapping(target = "categories", ignore = true),
    })
    UserModel dtoToUser(UserDto userDto);

}

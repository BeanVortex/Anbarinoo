package ir.darkdeveloper.anbarinoo.dto.mapper;

import ir.darkdeveloper.anbarinoo.dto.ChequeDto;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ChequeMapper {

    @Mappings({
            @Mapping(target = "userId", source = "model.user.id"),
            @Mapping(target = "issuedAt", dateFormat = "EE MMM dd yyyy HH:mm:ss"),
            @Mapping(target = "validTill", dateFormat = "EE MMM dd yyyy HH:mm:ss"),
            @Mapping(target = "createdAt", dateFormat = "EE MMM dd yyyy HH:mm:ss"),
            @Mapping(target = "updatedAt", dateFormat = "EE MMM dd yyyy HH:mm:ss")
    })
    ChequeDto chequeToDto(ChequeModel model);

}

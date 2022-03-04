package ir.darkdeveloper.anbarinoo.dto.mapper;

import ir.darkdeveloper.anbarinoo.dto.BuyDto;
import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BuyMapper {

    @Mappings({
            @Mapping(target = "productId", source = "model.product.id"),
            @Mapping(target = "createdAt", dateFormat = "EE MMM dd yyyy HH:mm:ss"),
            @Mapping(target = "updatedAt", dateFormat = "EE MMM dd yyyy HH:mm:ss")
    })
    BuyDto buyToDto(BuyModel model);

}

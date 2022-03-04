package ir.darkdeveloper.anbarinoo.dto.mapper;

import ir.darkdeveloper.anbarinoo.dto.BuyDto;
import ir.darkdeveloper.anbarinoo.dto.SellDto;
import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import ir.darkdeveloper.anbarinoo.model.Financial.SellModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BuySellMapper {

    @Mappings({
            @Mapping(target = "productId", source = "model.product.id"),
            @Mapping(target = "createdAt", dateFormat = "EE MMM dd yyyy HH:mm:ss"),
            @Mapping(target = "updatedAt", dateFormat = "EE MMM dd yyyy HH:mm:ss")
    })
    BuyDto buyToDto(BuyModel model);

    @Mappings({
            @Mapping(target = "productId", source = "model.product.id"),
            @Mapping(target = "createdAt", dateFormat = "EE MMM dd yyyy HH:mm:ss"),
            @Mapping(target = "updatedAt", dateFormat = "EE MMM dd yyyy HH:mm:ss")
    })
    SellDto sellToDto(SellModel model);

}

package ir.darkdeveloper.anbarinoo.dto.mapper;

import ir.darkdeveloper.anbarinoo.config.StartupConfig;
import ir.darkdeveloper.anbarinoo.dto.BuyDto;
import ir.darkdeveloper.anbarinoo.dto.SellDto;
import ir.darkdeveloper.anbarinoo.model.BuyModel;
import ir.darkdeveloper.anbarinoo.model.SellModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BuySellMapper {

    @Mappings({
            @Mapping(target = "productId", source = "model.product.id"),
            @Mapping(target = "createdAt", dateFormat = StartupConfig.DATE_FORMAT),
            @Mapping(target = "updatedAt", dateFormat = StartupConfig.DATE_FORMAT)
    })
    BuyDto buyToDto(BuyModel model);

    @Mappings({
            @Mapping(target = "productId", source = "model.product.id"),
            @Mapping(target = "createdAt", dateFormat = StartupConfig.DATE_FORMAT),
            @Mapping(target = "updatedAt", dateFormat = StartupConfig.DATE_FORMAT)
    })
    SellDto sellToDto(SellModel model);

}

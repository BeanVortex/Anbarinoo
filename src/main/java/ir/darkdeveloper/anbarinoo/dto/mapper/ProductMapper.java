package ir.darkdeveloper.anbarinoo.dto.mapper;

import ir.darkdeveloper.anbarinoo.dto.ProductDto;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mappings({
            @Mapping(target = "categoryId", source = "model.category.id"),
            @Mapping(target = "createdAt", dateFormat = "EE MMM dd yyyy HH:mm:ss"),
            @Mapping(target = "updatedAt", dateFormat = "EE MMM dd yyyy HH:mm:ss")
    })
    ProductDto productToDto(ProductModel model);


}

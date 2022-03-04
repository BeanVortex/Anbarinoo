package ir.darkdeveloper.anbarinoo.dto.mapper;

import ir.darkdeveloper.anbarinoo.dto.CategoryDto;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", imports = {ProductModel.class})
public interface CategoryMapper {

    @Mappings({
            @Mapping(target = "userId", source = "model.user.id"),
            @Mapping(target = "parentId", source = "model.parent.id"),
            @Mapping(target = "children", expression = "java(convertCategoriesToListOfId(model))"),
            @Mapping(target = "products", expression = "java(convertProductsToListOfId(model))")
    })
    CategoryDto categoryToDto(CategoryModel model);


    default List<Long> convertCategoriesToListOfId(CategoryModel model) {
        if (model.getChildren() != null)
            return model.getChildren().stream().map(CategoryModel::getId).toList();
        return Collections.emptyList();
    }

    default List<Long> convertProductsToListOfId(CategoryModel model) {
        if (model.getProducts() != null)
            return model.getProducts().stream().map(ProductModel::getId).toList();
        return Collections.emptyList();
    }
}

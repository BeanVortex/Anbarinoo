package ir.darkdeveloper.anbarinoo.util;

import ir.darkdeveloper.anbarinoo.exception.BadRequestException;
import ir.darkdeveloper.anbarinoo.model.CategoryModel;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public record ProductUtils(ProductRepository repo,
                           IOUtils ioUtils) {

    @Autowired
    public ProductUtils {
    }


    /**
     * preProduct will be updated
    * */
    public void validateAndUpdateProduct(Optional<ProductModel> product, ProductModel preProduct) {
        product.orElseThrow(() -> new BadRequestException("Product can't be null"));
        product.map(ProductModel::getId).ifPresent(id -> product.get().setId(null));
        preProduct.update(product.get());
    }

    /**
     * product will be updated
     * */
    public ProductModel updateProductImages(Optional<ProductModel> product, ProductModel preProduct) {
        product.map(ProductModel::getFiles)
                .orElseThrow(() -> new BadRequestException("Image files are empty"));
        ioUtils.addMoreProductImages(product.get(), preProduct);
        product.get().update(preProduct);
        return repo.save(product.get());
    }


    public ProductModel validateAndGetProduct(Optional<ProductModel> product) {
        var providedProduct = product.orElseThrow(() -> new BadRequestException("Product can't be null"));
        product.map(ProductModel::getCategory).map(CategoryModel::getId)
                .orElseThrow(() -> new BadRequestException("Product category or category id can't be empty"));
        product.get().setTax(product.map(ProductModel::getTax).orElse(9));
        return providedProduct;
    }
}

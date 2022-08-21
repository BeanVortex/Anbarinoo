package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.dto.ProductDto;
import ir.darkdeveloper.anbarinoo.dto.mapper.ProductMapper;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/category/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;
    private final ProductMapper mapper;

    @PostMapping("/save/")
    @PreAuthorize("hasAuthority('OP_ADD_PRODUCT')")
    public ResponseEntity<ProductDto> saveProduct(@ModelAttribute ProductModel product, HttpServletRequest request) {
        var savedProduct = service.saveProduct(Optional.ofNullable(product), request);
        return new ResponseEntity<>(mapper.productToDto(savedProduct), HttpStatus.CREATED);
    }

    @GetMapping("/search/")
    @PreAuthorize("hasAuthority('OP_ACCESS_PRODUCT')")
    public ResponseEntity<Page<ProductDto>> findByNameContains(@RequestParam String name, Pageable pageable,
                                                               HttpServletRequest request) {
        var products = service.findByNameContains(name, pageable, request);
        return ResponseEntity.ok(products.map(mapper::productToDto));
    }

    @PutMapping("/update/{id}/")
    @PreAuthorize("hasAuthority('OP_EDIT_PRODUCT')")
    public ResponseEntity<ProductDto> updateProduct(@RequestBody ProductModel product,
                                                    @PathVariable("id") Long productId,
                                                    HttpServletRequest request) {
        var updateProduct = service.updateProduct(Optional.ofNullable(product), productId, request);
        return ResponseEntity.ok(mapper.productToDto(updateProduct));
    }

    @PutMapping("/update/images/{id}/")
    @PreAuthorize("hasAuthority('OP_EDIT_PRODUCT')")
    public ResponseEntity<ProductDto> addNewProductImages(@ModelAttribute ProductModel product,
                                                          @PathVariable("id") Long productId,
                                                          HttpServletRequest request) {
        var updatedProduct = service.addNewProductImages(Optional.ofNullable(product), productId, request);
        return ResponseEntity.ok(mapper.productToDto(updatedProduct));
    }

    @PutMapping("/update/delete-images/{id}/")
    @PreAuthorize("hasAuthority('OP_EDIT_PRODUCT')")
    public ResponseEntity<String> deleteProductImages(@RequestBody ProductModel product,
                                                      @PathVariable("id") Long productId,
                                                      HttpServletRequest request) {
        var message = service.deleteProductImages(Optional.ofNullable(product), productId, request);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_ACCESS_PRODUCT')")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(mapper.productToDto(service.getProduct(id, request)));
    }

    @GetMapping("/user/")
    @PreAuthorize("hasAuthority('OP_ACCESS_PRODUCT')")
    public ResponseEntity<Page<ProductDto>> getAllProductsOfUser(Pageable pageable,
                                                                 HttpServletRequest request) {
        return ResponseEntity.ok(service.getAllProductsOfUser(pageable, request).map(mapper::productToDto));
    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAuthority('OP_DELETE_PRODUCT')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(service.deleteProduct(id, request));
    }


}

package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.dto.ProductDto;
import ir.darkdeveloper.anbarinoo.dto.mapper.ProductMapper;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<ProductDto> saveProduct(@ModelAttribute ProductModel product,
                                                  HttpServletRequest request) {
        return ResponseEntity.ok(mapper.productToDto(service.saveProduct(Optional.ofNullable(product), request)));
    }

    @GetMapping("/search/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<Page<ProductDto>> findByNameContains(@RequestParam String name, Pageable pageable,
                                                               HttpServletRequest request) {
        return ResponseEntity.ok(service.findByNameContains(name, pageable, request).map(mapper::productToDto));
    }

    @PutMapping("/update/{id}/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<ProductDto> updateProduct(@RequestBody ProductModel product, @PathVariable("id") Long productId,
                                                    HttpServletRequest request) {
        return ResponseEntity.ok(mapper.productToDto(
                service.updateProduct(Optional.ofNullable(product), productId, request)
        ));
    }

    @PutMapping("/update/images/{id}/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<ProductDto> updateProductImages(@ModelAttribute ProductModel product, @PathVariable("id") Long productId,
                                                          HttpServletRequest request) {
        return ResponseEntity.ok(mapper.productToDto(
                service.updateProductImages(Optional.ofNullable(product), productId, request)));
    }

    @PutMapping("/update/delete-images/{id}/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<?> updateDeleteProductImages(@RequestBody ProductModel product, @PathVariable("id") Long productId,
                                                       HttpServletRequest request) {
        return service.updateDeleteProductImages(Optional.ofNullable(product), productId, request);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(mapper.productToDto(service.getProduct(id, request)));
    }

    @GetMapping("/user/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable,
                                                           HttpServletRequest request) {
        return ResponseEntity.ok(service.getAllProducts(pageable, request).map(mapper::productToDto));
    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_ADMIN','OP_ACCESS_USER')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        return service.deleteProduct(id, request);
    }


}

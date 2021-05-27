package ir.darkdeveloper.anbarinoo.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository repo;

    @Autowired
    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public ProductModel saveProduct(ProductModel productModel) {
        try {
            // String fileName = saveFile(productModel.getFile());
            // if (fileName != null) {
            //     productModel.setImage(fileName);
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return repo.save(productModel);
    }

    public static String saveFile(MultipartFile file) throws IOException {
        if (file != null) {
            String path = ResourceUtils.getFile("classpath:static/img").getAbsolutePath();
            byte[] bytes = file.getBytes();
            String fileName = UUID.randomUUID() + "." + Objects.requireNonNull(file.getContentType()).split("/")[1];
            Files.write(Paths.get(path + File.separator + fileName), bytes);
            return fileName;
        }else {
            return null;
        }
    }

    public Page<ProductModel> findByNameContains(String name, Pageable pageable){
        return repo.findByNameContains(name, pageable);
    }


}

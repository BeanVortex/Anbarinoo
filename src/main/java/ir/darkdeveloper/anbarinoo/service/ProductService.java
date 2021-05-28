package ir.darkdeveloper.anbarinoo.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.repository.ProductRepository;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;

@Service
public class ProductService {
    private final ProductRepository repo;
    private final JwtUtils jwtUtils;

    @Autowired
    public ProductService(ProductRepository repo, JwtUtils jwtUtils) {
        this.repo = repo;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public ProductModel saveProduct(ProductModel productModel, HttpServletRequest request) {
        try {
            String refreshToken = request.getHeader("refresh_token");
            Long userId = ((Integer) jwtUtils.getAllClaimsFromToken(refreshToken).get("user_id")).longValue();
            productModel.setUser(new UserModel(userId));

            List<String> fileNames = saveFiles(productModel.getFiles());
            if (!fileNames.isEmpty()) {
                productModel.setImages(fileNames);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return repo.save(productModel);
    }

    private List<String> saveFiles(List<MultipartFile> files) throws IOException {
        List<String> fileNames = new ArrayList<>();
        
        for (MultipartFile file : files) {     
            if (file != null) {
                String path = ResourceUtils.getFile("classpath:static/user/product_images").getAbsolutePath();
                byte[] bytes = file.getBytes();
                String fileName = UUID.randomUUID() + "." + Objects.requireNonNull(file.getContentType()).split("/")[1];
                Files.write(Paths.get(path + File.separator + fileName), bytes);
                fileNames.add(fileName);
            }
        }
        return fileNames;
    }

    public Page<ProductModel> findByNameContains(String name, Pageable pageable) {
        return repo.findByNameContains(name, pageable);
    }

}

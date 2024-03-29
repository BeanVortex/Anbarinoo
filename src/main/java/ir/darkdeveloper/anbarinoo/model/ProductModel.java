package ir.darkdeveloper.anbarinoo.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name = "products")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductModel implements UpdateModel<ProductModel> {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    private String description;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id", referencedColumnName = "id"))
    private List<String> images;

    @Transient
    private List<MultipartFile> files;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer tax;

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    @ToString.Exclude
    private List<SellModel> sells = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    @ToString.Exclude
    private List<BuyModel> buys = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_id")
    @ToString.Exclude
    private CategoryModel category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;


    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalCount;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ProductModel(Long id) {
        this.id = id;
    }

    @Override
    public void update(ProductModel model) {
        id = model.id != null || id == null ? model.id : id;
        name = model.name != null || name == null ? model.name : name;
        price = model.price != null || price == null ? model.price : price;
        description = model.description != null || description == null ? model.description : description;
        category = model.category != null || category == null ? model.category : category;
        totalCount = model.totalCount != null || totalCount == null ? model.totalCount : totalCount;
        tax = model.tax != null || tax == null ? model.tax : tax;
    }

    @PreRemove
    public void preRemove() {
        buys.forEach(b -> b.setProduct(null));
        sells.forEach(s -> s.setProduct(null));
    }
}


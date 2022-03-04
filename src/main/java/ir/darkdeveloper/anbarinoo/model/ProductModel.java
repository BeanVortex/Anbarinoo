package ir.darkdeveloper.anbarinoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;

import ir.darkdeveloper.anbarinoo.model.Financial.BuyModel;
import ir.darkdeveloper.anbarinoo.model.Financial.SellModel;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.web.multipart.MultipartFile;

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

    private Long firstBuyId;

    @Builder.Default
    private Boolean canUpdate = true;

    @Column(length = 50, nullable = false)
    private String name;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id", referencedColumnName = "id"))
    private List<String> images;

    @Transient
    private List<MultipartFile> files;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer tax;

    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<SellModel> sells;

    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<BuyModel> buys;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cat_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CategoryModel category;


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
}


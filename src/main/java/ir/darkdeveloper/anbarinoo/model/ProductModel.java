package ir.darkdeveloper.anbarinoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;

import ir.darkdeveloper.anbarinoo.model.Financial.BuysModel;
import ir.darkdeveloper.anbarinoo.model.Financial.SellsModel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name = "products")
@ToString(exclude = "category")
@EqualsAndHashCode(exclude = "category")
@NoArgsConstructor
public class ProductModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id", referencedColumnName = "id"))
    private List<String> images;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<MultipartFile> files;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<SellsModel> sells;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<BuysModel> buys;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cat_id")
    private CategoryModel category;


    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ProductModel(Long id) {
        this.id = id;
    }

    public void update(ProductModel other) {
        id = other.id != null || id == null ? other.id : id;
        name = other.name != null || name == null ? other.name : name;
        price = other.price != null || price == null ? other.price : price;
        description = other.description != null || description == null ? other.description : description;
        category = other.category != null || category == null ? other.category : category;
        totalCount = other.totalCount != null || totalCount == null ? other.totalCount : totalCount;
    }
}


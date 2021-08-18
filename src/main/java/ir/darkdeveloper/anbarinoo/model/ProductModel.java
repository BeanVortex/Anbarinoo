package ir.darkdeveloper.anbarinoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;

import ir.darkdeveloper.anbarinoo.model.Financial.BuysModel;
import ir.darkdeveloper.anbarinoo.model.Financial.SellsModel;
import lombok.EqualsAndHashCode;
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

    private BigDecimal price;


    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<SellsModel> sells;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<BuysModel> buys;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userName")
    @JsonIdentityReference(alwaysAsId = true)
    private UserModel user;

    @ManyToOne
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


    public void merge(ProductModel other) {
        id = other.id == null ? id : other.id;
        name = other.name == null ? name : other.name;
        description = other.description == null ? description : other.description;
        category = other.category == null ? category : other.category;
        totalCount = other.totalCount == null ? totalCount : other.totalCount;
        createdAt = other.createdAt == null ? createdAt : other.createdAt;
        updatedAt = other.updatedAt == null ? updatedAt : other.updatedAt;
    }
}


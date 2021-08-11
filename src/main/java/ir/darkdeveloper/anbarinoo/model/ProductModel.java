package ir.darkdeveloper.anbarinoo.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

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

    private Double price;

    @Column(name = "buy_price")
    private Double buyPrice;

    @Column(name = "sell_count")
    private Integer soldCount;

    @Column(name = "buy_count")
    private Integer boughtCount;

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
        price = other.price == null ? price : other.price;
        buyPrice = other.buyPrice == null ? buyPrice : other.buyPrice;
        soldCount = other.soldCount == null ? soldCount : other.soldCount;
        boughtCount = other.boughtCount == null ? boughtCount : other.boughtCount;
        category = other.category == null ? category : other.category;
        totalCount = other.totalCount == null ? totalCount : other.totalCount;
        createdAt = other.createdAt == null ? createdAt : other.createdAt;
        updatedAt = other.updatedAt == null ? updatedAt : other.updatedAt;
    }
}


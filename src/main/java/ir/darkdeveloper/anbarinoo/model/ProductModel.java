package ir.darkdeveloper.anbarinoo.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name = "products")
public class ProductModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    private String description;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "id"))
    private List<String> images;

    @Transient
    private List<MultipartFile> files;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, name = "sell_price")
    private Double sellPrice;

    @Column(nullable = false, name = "buy_price")
    private Double buyPrice;

    @Column(nullable = false, name = "sell_count")
    private int soldCount;

    @Column(nullable = false, name = "buy_count")
    private int boughtCount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;


    @Column(nullable = false, name = "total_count")
    private int totalCount;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;


}

package ir.darkdeveloper.anbarinoo.model.Financial;

import ir.darkdeveloper.anbarinoo.model.ProductModel;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sells")
public class SellsModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal count;

    // precision means the whole numbers contains in decimal or integer
    // scale means the count of numbers after . or point
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer tax = 9;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductModel product;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void update(SellsModel other) {
        count = other.count != null && count != null ? other.count : count;
        price = other.price != null && price != null ? other.price : price;
    }
}

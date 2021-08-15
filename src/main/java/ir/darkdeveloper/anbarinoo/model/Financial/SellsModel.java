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

    @Column(nullable = false)
    private BigInteger count;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer tax = 9;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @Column(nullable = false)
    private ProductModel product;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

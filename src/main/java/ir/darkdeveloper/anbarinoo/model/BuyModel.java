package ir.darkdeveloper.anbarinoo.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@ToString
@Entity
@Table(name = "buys")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuyModel implements UpdateModel<BuyModel> {

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
    @Builder.Default
    private Integer tax = 9;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductModel product;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private UserModel user;

    private String productName;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public void update(BuyModel model) {
        price = model.price != null || price == null ? model.price : price;
        count = model.count != null || count == null ? model.count : count;
        tax = model.tax != null || tax == null ? model.tax : tax;
    }

}



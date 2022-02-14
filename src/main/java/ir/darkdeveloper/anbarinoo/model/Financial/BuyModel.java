package ir.darkdeveloper.anbarinoo.model.Financial;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.model.UpdateModel;
import ir.darkdeveloper.anbarinoo.model.deserializers.BuyDeserializer;
import ir.darkdeveloper.anbarinoo.model.serializers.BuySerialize;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@ToString
@Entity
@Table(name = "buys")
@JsonDeserialize(using = BuyDeserializer.class)
@JsonSerialize(using = BuySerialize.class)
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
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private ProductModel product;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public void update(BuyModel model) {
        count = model.count != null || count == null ? model.count : count;
        price = model.price != null || price == null ? model.price : price;
        tax = model.tax != null || tax == null ? model.tax : tax;
    }

}



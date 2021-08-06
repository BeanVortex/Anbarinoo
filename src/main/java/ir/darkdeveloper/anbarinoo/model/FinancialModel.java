package ir.darkdeveloper.anbarinoo.model;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.*;

import lombok.Data;

@Data
@Entity
@Table(name = "financial")
public class FinancialModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private BigDecimal earnings = new BigDecimal(0);

    @Column(nullable = false)
    private BigDecimal costs = new BigDecimal(0);
    ;

    @Column(nullable = false)
    private BigDecimal profit = new BigDecimal(0);
    ;

    @Column(nullable = false)
    private Integer tax = 9;

    @OneToMany(mappedBy = "financial")
    private List<UserModel> users;

}

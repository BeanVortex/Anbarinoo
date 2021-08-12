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


    @Column(nullable = false)
    private BigDecimal profit = new BigDecimal(0);


    @Column(nullable = false)
    private Integer tax = 9;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserModel user;

    public void update(FinancialModel other) {
        id = other.id != null && id != null ? other.id : id;
        earnings = other.earnings != null && earnings != null ? other.earnings : earnings;
        costs = other.costs != null && costs != null ? other.costs : costs;
        profit = other.profit != null && profit != null ? other.profit : profit;
        tax = other.tax != null && tax != null ? other.tax : tax;
    }
}

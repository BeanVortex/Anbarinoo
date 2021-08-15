package ir.darkdeveloper.anbarinoo.model.Financial;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import ir.darkdeveloper.anbarinoo.model.UserModel;
import lombok.Data;

@Data
@Entity
@Table(name = "debts_demands")
public class DebtOrDemandModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    private Boolean isDebt = false;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    private LocalDateTime deadline;

}

package ir.darkdeveloper.anbarinoo.model.Financial;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.*;

import ir.darkdeveloper.anbarinoo.model.UserModel;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name = "cheques")
public class ChequeModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String nameOf;

    @Column(nullable = false)
    private String payTo;

    @Column(nullable = false)
    private BigDecimal amount;

    private Boolean isDebt = false;

    private Boolean isCheckedOut = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime validTill;

}
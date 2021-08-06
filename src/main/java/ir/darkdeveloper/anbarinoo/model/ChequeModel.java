package ir.darkdeveloper.anbarinoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
    private UserModel user;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime validTill;

}
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.model.deserializers.DebtOrDemandDeserializer;
import ir.darkdeveloper.anbarinoo.model.serializers.DebtOrDemandSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "debts_demands")
@JsonDeserialize(using = DebtOrDemandDeserializer.class)
@JsonSerialize(using = DebtOrDemandSerializer.class)
@AllArgsConstructor
@NoArgsConstructor
public class DebtOrDemandModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String nameOf;

    @Column(nullable = false)
    private String payTo;

    @Column(nullable = false)
    private Boolean isDebt = false;

    @Column(nullable = false)
    private Boolean isCheckedOut = false;


    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    private Long chequeId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime validTill;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void update(DebtOrDemandModel other) {
        id = other.id != null || id == null ? other.id : id;
        nameOf = other.nameOf != null || nameOf == null ? other.nameOf : nameOf;
        payTo = other.payTo != null || payTo == null ? other.payTo : payTo;
        isDebt = other.isDebt != null || isDebt == null ? other.isDebt : isDebt;
        isCheckedOut = other.isCheckedOut != null || isCheckedOut == null ? other.isCheckedOut : isCheckedOut;
        amount = other.amount != null || amount == null ? other.amount : amount;
        issuedAt = other.issuedAt != null || issuedAt == null ? other.issuedAt : issuedAt;
        validTill = other.validTill != null || validTill == null ? other.validTill : validTill;
    }
}

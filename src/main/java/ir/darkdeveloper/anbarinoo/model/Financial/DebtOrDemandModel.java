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
import ir.darkdeveloper.anbarinoo.model.UpdateModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.model.deserializers.DebtOrDemandDeserializer;
import ir.darkdeveloper.anbarinoo.model.serializers.DebtOrDemandSerializer;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "debts_demands")
@JsonDeserialize(using = DebtOrDemandDeserializer.class)
@JsonSerialize(using = DebtOrDemandSerializer.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DebtOrDemandModel implements UpdateModel<DebtOrDemandModel> {

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

    @Override
    public void update(DebtOrDemandModel model) {
        id = model.id != null || id == null ? model.id : id;
        nameOf = model.nameOf != null || nameOf == null ? model.nameOf : nameOf;
        payTo = model.payTo != null || payTo == null ? model.payTo : payTo;
        isDebt = model.isDebt != null || isDebt == null ? model.isDebt : isDebt;
        isCheckedOut = model.isCheckedOut != null || isCheckedOut == null ? model.isCheckedOut : isCheckedOut;
        amount = model.amount != null || amount == null ? model.amount : amount;
        issuedAt = model.issuedAt != null || issuedAt == null ? model.issuedAt : issuedAt;
        validTill = model.validTill != null || validTill == null ? model.validTill : validTill;
    }
}

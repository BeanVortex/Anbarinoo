package ir.darkdeveloper.anbarinoo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.darkdeveloper.anbarinoo.util.json.DebtOrDemandDeserializer;
import ir.darkdeveloper.anbarinoo.util.json.DebtOrDemandSerializer;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@ToString
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
    @Builder.Default
    private Boolean isDebt = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCheckedOut = false;


    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    private Long chequeId;

    @ManyToOne(fetch = FetchType.LAZY)
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
        nameOf = model.nameOf != null || nameOf == null ? model.nameOf : nameOf;
        id = model.id != null || id == null ? model.id : id;
        payTo = model.payTo != null || payTo == null ? model.payTo : payTo;
        isDebt = model.isDebt != null || isDebt == null ? model.isDebt : isDebt;
        isCheckedOut = model.isCheckedOut != null || isCheckedOut == null ? model.isCheckedOut : isCheckedOut;
        amount = model.amount != null || amount == null ? model.amount : amount;
        issuedAt = model.issuedAt != null || issuedAt == null ? model.issuedAt : issuedAt;
        validTill = model.validTill != null || validTill == null ? model.validTill : validTill;
    }
}

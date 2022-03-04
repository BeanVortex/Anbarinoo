package ir.darkdeveloper.anbarinoo.model.Financial;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.darkdeveloper.anbarinoo.model.UpdateModel;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.model.deserializers.ChequeDeserializer;
import ir.darkdeveloper.anbarinoo.model.serializers.ChequeSerializer;
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
@Table(name = "cheques")
@JsonDeserialize(using = ChequeDeserializer.class)
@JsonSerialize(using = ChequeSerializer.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChequeModel implements UpdateModel<ChequeModel> {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String nameOf;

    @Column(nullable = false)
    private String payTo;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDebt = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCheckedOut = false;

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
    public void update(ChequeModel mode) {
        id = mode.id != null || id == null ? mode.id : id;
        nameOf = mode.nameOf != null || nameOf == null ? mode.nameOf : nameOf;
        payTo = mode.payTo != null || payTo == null ? mode.payTo : payTo;
        amount = mode.amount != null || amount == null ? mode.amount : amount;
        isDebt = mode.isDebt != null || isDebt == null ? mode.isDebt : isDebt;
        isCheckedOut = mode.isCheckedOut != null || isCheckedOut == null ? mode.isCheckedOut : isCheckedOut;
        issuedAt = mode.issuedAt != null || issuedAt == null ? mode.issuedAt : issuedAt;
        validTill = mode.validTill != null || validTill == null ? mode.validTill : validTill;
    }

}
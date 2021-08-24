package ir.darkdeveloper.anbarinoo.model.Financial;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ir.darkdeveloper.anbarinoo.model.UserModel;
import ir.darkdeveloper.anbarinoo.model.deserializers.ChequeDeserializer;
import ir.darkdeveloper.anbarinoo.model.serializers.ChequeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cheques")
@JsonDeserialize(using = ChequeDeserializer.class)
@JsonSerialize(using = ChequeSerializer.class)
@AllArgsConstructor
@NoArgsConstructor
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
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class)
    @JsonIdentityReference(alwaysAsId = true)
    private UserModel user;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime validTill;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public void update(ChequeModel other) {
        id = other.id != null || id == null ? other.id : id;
        nameOf = other.nameOf != null || nameOf == null ? other.nameOf : nameOf;
        payTo = other.payTo != null || payTo == null ? other.payTo : payTo;
        amount = other.amount != null || amount == null ? other.amount : amount;
        isDebt = other.isDebt != null || isDebt == null ? other.isDebt : isDebt;
        isCheckedOut = other.isCheckedOut != null || isCheckedOut == null ? other.isCheckedOut : isCheckedOut;
        issuedAt = other.issuedAt != null || issuedAt == null ? other.issuedAt : issuedAt;
        validTill = other.validTill != null || validTill == null ? other.validTill : validTill;
    }

}
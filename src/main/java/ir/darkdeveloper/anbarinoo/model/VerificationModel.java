package ir.darkdeveloper.anbarinoo.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "email_verification")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class VerificationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String token;

    @OneToOne(targetEntity = UserModel.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserModel user;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Format that js Date object understand
    @JsonFormat(pattern = "EE MMM dd yyyy HH:mm:ss")
    private LocalDateTime expiresAt;

    @JsonFormat(pattern = "EE MMM dd yyyy HH:mm:ss")
    private LocalDateTime verifiedAt;

    public VerificationModel(String token, UserModel user, LocalDateTime expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
    }
}
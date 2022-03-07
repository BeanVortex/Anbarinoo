package ir.darkdeveloper.anbarinoo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "tokens")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String accessToken;
}

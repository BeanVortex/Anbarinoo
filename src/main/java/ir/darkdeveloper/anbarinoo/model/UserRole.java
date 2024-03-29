package ir.darkdeveloper.anbarinoo.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ElementCollection(targetClass = Authority.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "authorities",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    @Enumerated(EnumType.STRING)
    private List<Authority> authorities;

    // it means, users own roles
    // other side is the owner
    // this is the inverse side
    @ManyToMany(mappedBy = "roles")
    @ToString.Exclude
    private List<UserModel> users;

    public UserRole(Long id, String name, List<Authority> authorities) {
        this.id = id;
        this.name = name;
        this.authorities = authorities;
    }

}

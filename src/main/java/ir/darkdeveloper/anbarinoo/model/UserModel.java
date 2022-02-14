package ir.darkdeveloper.anbarinoo.model;

import com.fasterxml.jackson.annotation.*;
import ir.darkdeveloper.anbarinoo.model.Auth.AuthProvider;
import ir.darkdeveloper.anbarinoo.model.Auth.Authority;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "users")
@JsonIgnoreProperties(value = "attributes")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserModel implements UserDetails, OAuth2User, UpdateModel<UserModel> {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    @NotEmpty
    @Email
    private String email;

    @Column(unique = true)
    private String userName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9\\s]).+", message = "Bad password")
    @Size(min = 6, message = "Password length must be at least 6")
    private String password;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9\\s]).+", message = "Bad repeat password")
    @Size(min = 6, message = "Password length must be at least 6")
    private String passwordRepeat;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String prevPassword;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private Boolean enabled;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile shopFile;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String shopImage;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile profileFile;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String profileImage;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
    @JsonIdentityReference(alwaysAsId = true)
    private Set<UserRole> roles;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String shopName;

    private String address;

    private String description;

    @OneToMany(mappedBy = "user")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ToString.Exclude
    private List<DebtOrDemandModel> debtOrDemand;

    @OneToMany(mappedBy = "user")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ToString.Exclude
    private List<ChequeModel> cheques;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ToString.Exclude
    private List<CategoryModel> categories;

    // For saving products I need it
    public UserModel(Long id) {
        this.id = id;
    }


    public UserModel(Long id, String email, String userName, Boolean enabled, String shopImage,
                     String profileImage, String shopName, LocalDateTime createdAt, LocalDateTime updatedAt,
                     String address, String description, AuthProvider provider) {
        this.id = id;
        this.email = email;
        this.userName = userName;
        this.enabled = enabled;
        this.shopImage = shopImage;
        this.profileImage = profileImage;
        this.shopName = shopName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.address = address;
        this.description = description;
        this.provider = provider;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> auth = new ArrayList<>();
        if (roles != null)
            roles.forEach(e -> auth.addAll(e.getAuthorities()));
        else
            auth.add(Authority.OP_ACCESS_USER);
        return auth;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Map<String, Object> getAttributes() {

        return new HashMap<>();
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public void update(UserModel model) {
        id = model.id != null || id == null ? model.id : id;
        userName = model.userName != null || userName == null ? model.userName : userName;
        provider = model.provider != null || provider == null ? model.provider : provider;
        enabled = model.enabled != null || enabled == null ? model.enabled : enabled;
        shopImage = model.shopImage != null || shopImage == null ? model.shopImage : shopImage;
        profileImage = model.profileImage != null || profileImage == null ? model.profileImage : profileImage;
        createdAt = model.createdAt != null || createdAt == null ? model.createdAt : createdAt;
        updatedAt = model.updatedAt != null || updatedAt == null ? model.updatedAt : updatedAt;
        shopName = model.shopName != null || shopName == null ? model.shopName : shopName;
        address = model.address != null || address == null ? model.address : address;
        description = model.description != null || description == null ? model.description : description;
    }
}

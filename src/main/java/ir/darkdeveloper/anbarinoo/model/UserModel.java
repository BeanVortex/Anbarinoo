package ir.darkdeveloper.anbarinoo.model;

import com.fasterxml.jackson.annotation.*;
import ir.darkdeveloper.anbarinoo.model.Auth.AuthProvider;
import ir.darkdeveloper.anbarinoo.model.Auth.Authority;
import ir.darkdeveloper.anbarinoo.model.Financial.ChequeModel;
import ir.darkdeveloper.anbarinoo.model.Financial.DebtOrDemandModel;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@Table(name = "users")
@JsonIgnoreProperties(value = "attributes")
@ToString(exclude = {"categories"})
public class UserModel implements UserDetails, OAuth2User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Column(unique = true)
    private String userName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
    private List<DebtOrDemandModel> debtOrDemand;

    @OneToMany(mappedBy = "user")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private List<ChequeModel> cheques;

    @OneToMany(mappedBy = "user")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private List<CategoryModel> categories;

    // For saving products I need it
    public UserModel(Long id) {
        this.id = id;
    }

    public UserModel() {
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

    public void merge(UserModel other) {
        id = other.id == null ? id : other.id;
        userName = other.userName == null ? userName : other.userName;
        password = other.password == null ? password : other.password;
        passwordRepeat = other.passwordRepeat == null ? passwordRepeat : other.passwordRepeat;
        prevPassword = other.prevPassword == null ? prevPassword : other.prevPassword;
        provider = other.provider == null ? provider : other.provider;
        enabled = other.enabled == null ? enabled : other.enabled;
        shopImage = other.shopImage == null ? shopImage : other.shopImage;
        profileImage = other.profileImage == null ? profileImage : other.profileImage;
        createdAt = other.createdAt == null ? createdAt : other.createdAt;
        updatedAt = other.updatedAt == null ? updatedAt : other.updatedAt;
        shopName = other.shopName == null ? shopName : other.shopName;
        address = other.address == null ? address : other.address;
        description = other.description == null ? description : other.description;
    }

    public void update(UserModel other) {
        id = other.id != null || id == null ? other.id : id;
        userName = other.userName != null || userName == null ? other.userName : userName;
        provider = other.provider != null || provider == null ? other.provider : provider;
        enabled = other.enabled != null || enabled == null ? other.enabled : enabled;
        shopImage = other.shopImage != null || shopImage == null ? other.shopImage : shopImage;
        profileImage = other.profileImage != null || profileImage == null ? other.profileImage : profileImage;
        createdAt = other.createdAt != null || createdAt == null ? other.createdAt : createdAt;
        updatedAt = other.updatedAt != null || updatedAt == null ? other.updatedAt : updatedAt;
        shopName = other.shopName != null || shopName == null ? other.shopName : shopName;
        address = other.address != null || address == null ? other.address : address;
        description = other.description != null || description == null ? other.description : description;
    }
}

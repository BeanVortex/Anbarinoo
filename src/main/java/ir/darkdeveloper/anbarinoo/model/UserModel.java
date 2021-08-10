package ir.darkdeveloper.anbarinoo.model;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
@Entity
@Table(name = "users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties(value = "attributes")
@DynamicUpdate
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
    private Set<UserRoles> roles;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String shopName;


    private String address;

    private String description;

    @OneToOne
    @JoinColumn(name = "financial_id")
    private FinancialModel financial;

    @OneToMany(mappedBy = "user")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private List<DebtOrDemandModel> debtOrDemand;

    @OneToMany(mappedBy = "user")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private List<ChequeModel> cheques;

    //mappedBy is read-only. can't add product while creating user
    @OneToMany(mappedBy = "user")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private List<ProductModel> products;

    @OneToMany(mappedBy = "user")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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
        email = other.email == null ? email : other.email;
        userName = other.userName == null ? userName : other.userName;
        password = other.password == null ? password : other.password;
        passwordRepeat = other.passwordRepeat == null ? passwordRepeat : other.passwordRepeat;
        prevPassword = other.prevPassword == null ? prevPassword : other.prevPassword;
        provider = other.provider == null ? provider : other.provider;
        enabled = other.enabled == null ? enabled : other.enabled;
        shopFile = other.shopFile == null ? shopFile : other.shopFile;
        shopImage = other.shopImage == null ? shopImage : other.shopImage;
        profileFile = other.profileFile == null ? profileFile : other.profileFile;
        profileImage = other.profileImage == null ? profileImage : other.profileImage;
        createdAt = other.createdAt == null ? createdAt : other.createdAt;
        updatedAt = other.updatedAt == null ? updatedAt : other.updatedAt;
        shopName = other.shopName == null ? shopName : other.shopName;
        address = other.address == null ? address : other.address;
        description = other.description == null ? description : other.description;
        financial = other.financial == null ? financial : other.financial;
        if (other.cheques != null)
            cheques.addAll(other.cheques);
        if (other.debtOrDemand != null)
            debtOrDemand.addAll(other.debtOrDemand);
        if (other.products != null)
            products.addAll(other.products);
        if (other.categories != null)
            categories.addAll(other.categories);
    }

}

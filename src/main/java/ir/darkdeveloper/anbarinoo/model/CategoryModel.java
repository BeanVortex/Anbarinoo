package ir.darkdeveloper.anbarinoo.model;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "categories")
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
public class CategoryModel {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;


    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private UserModel user;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private CategoryModel parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private List<CategoryModel> children = new LinkedList<>();

    public CategoryModel(String name) {
        this.name = name;
    }

    public CategoryModel(String name, CategoryModel parent) {
        this.name = name;
        this.parent = parent;
    }

    public void addChild(CategoryModel children) {
        this.children.add(children);
    }

}


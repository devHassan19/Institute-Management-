package com.example.institute.institute.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
@ToString(exclude = {"password" , "userProfile" , "recipeList", "categoryList"})

public class User {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String emailAddress;

    @Column
    @JsonIgnore
    private Boolean enabled = false;

    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @OneToOne(cascade = CascadeType.ALL,fetch =  FetchType.LAZY)
    @JoinColumn(name ="profile_id" , referencedColumnName = "id")
    private  UserProfile userProfile;

//    @OneToMany(mappedBy = "user" , fetch = FetchType.LAZY)
//    private List<Category> categoryList;

    @JsonIgnore
    public String getPassword() {
        return password;
    }

}

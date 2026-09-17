package com.example.demo.entity;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Categories")
public class Category implements Serializable{
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    
    @Column(name = "category_name", columnDefinition = "nvarchar(255)")
    private String categoryName;
    
    private String icon;

    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL )
    private Set<Product> products;
}

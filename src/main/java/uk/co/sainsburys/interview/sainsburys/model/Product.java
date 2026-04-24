package uk.co.sainsburys.interview.sainsburys.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

@Entity(name = "product")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@NaturalIdCache  // Enables caching for @NaturalId lookups
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_uid", nullable = false, updatable = false, unique = true)
    private Integer product_uid;

    @Column
    String product_type;
    @Column
    String name;
    @Column
    String full_url;

}

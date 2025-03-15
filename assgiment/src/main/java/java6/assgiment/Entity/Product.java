package java6.assgiment.Entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(255)")
    private String classify;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "VARCHAR(255)")
    private String nameProduct;

    @Column(columnDefinition = "VARCHAR(255)")
    private String photo;

    @Column(columnDefinition = "DOUBLE")
    private double price;
}

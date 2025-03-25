package java6.assgiment.Entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", length = 255, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private Role role;

    @Column(name = "img", length = 255)
    private String img;

    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(name = "ward", length = 255)
    private String ward;

    @Column(name = "district", length = 255)
    private String district;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> orders;

    public enum Role {
        USER, ADMIN;

        boolean startsWith(String string) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'startsWith'");
        }
    }
}
package java6.assgiment.Entity; // Sửa "assgiment" thành "assignment"

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "contact") // Đổi thành chữ thường
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 255, nullable = false)
    private String username;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
}
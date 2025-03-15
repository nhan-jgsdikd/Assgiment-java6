    package java6.assgiment.Entity; 

    import jakarta.persistence.*;
    import lombok.*;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Entity
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(columnDefinition = "VARCHAR(255)")
        private String username;

        @Column(columnDefinition = "VARCHAR(255)")
        private String password;

        @Column(columnDefinition = "VARCHAR(255)")
        private String email;
        
        @Column(columnDefinition = "VARCHAR(255)")
        private String role;

        @Column(columnDefinition = "VARCHAR(255)")
        private String img;

    }
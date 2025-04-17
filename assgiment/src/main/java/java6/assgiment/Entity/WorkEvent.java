package java6.assgiment.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "work_events")
public class WorkEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Column(nullable = false, length = 255)
    private String title;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Trạng thái không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EventStatus status;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public enum EventStatus {
        PENDING, COMPLETED, CANCELLED
    }
}
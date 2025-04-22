package java6.assgiment.Entity; // Sửa "assgiment" thành "assignment"

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code; // Mã voucher duy nhất (VD: "DISCOUNT10")

    @Column(name = "discount_value", nullable = false)
    private Double discountValue; // Giá trị giảm giá (VD: 10% hoặc 100.000 VNĐ)

    @Column(name = "is_percentage", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPercentage; // Xác định giảm giá theo phần trăm hay số tiền cố định

    @Column(name = "min_order_value")
    private Double minOrderValue; // Giá trị đơn hàng tối thiểu để áp dụng voucher

    @Column(name = "max_discount")
    private Double maxDiscount; // Giới hạn mức giảm tối đa (nếu là phần trăm)

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate; // Ngày bắt đầu hiệu lực

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate; // Ngày hết hạn

    @Column(name = "quantity", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer quantity; // Số lượng voucher có thể sử dụng

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted; // Trạng thái xóa mềm

    // Quan hệ với User (nếu mỗi user chỉ được dùng voucher 1 lần)
    @ManyToMany(mappedBy = "vouchers")
    private List<User> users;

    // Quan hệ với Orders (nếu voucher áp dụng cho đơn hàng)
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL)
    private List<Orders> orders;
}
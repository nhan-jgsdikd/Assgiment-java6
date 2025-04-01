package java6.assgiment.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "payment_method", length = 255, nullable = false)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private OrderStatus status;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted;

    @Column(name = "shipping_address_line", length = 255)
    private String shippingAddressLine;

    @Column(name = "shipping_ward", length = 255)
    private String shippingWard;

    @Column(name = "shipping_district", length = 255)
    private String shippingDistrict;

    @Column(name = "shipping_city", length = 255)
    private String shippingCity;

    // Sửa từ OrderDetailDAO thành OrderDetail
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderDetail> orderDetails;

    // Thêm mối quan hệ với Voucher
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    public enum OrderStatus {
        PREPARING, SHIPPING, DELIVERED, CANCELLED
<<<<<<< HEAD
    }

    // Phương thức tính tổng tiền sau khi áp dụng voucher
    public Double getFinalAmount() {
        if (voucher == null) {
            return totalAmount;
        }

        Double discount = 0.0;
        if (voucher.getIsPercentage()) {
            discount = totalAmount * (voucher.getDiscountValue() / 100);
            if (voucher.getMaxDiscount() != null && discount > voucher.getMaxDiscount()) {
                discount = voucher.getMaxDiscount();
            }
        } else {
            discount = voucher.getDiscountValue();
        }

        return totalAmount - discount < 0 ? 0 : totalAmount - discount;
=======
>>>>>>> 5b7f43e002990d06e1b784d451983dd72f0b2a31
    }
}
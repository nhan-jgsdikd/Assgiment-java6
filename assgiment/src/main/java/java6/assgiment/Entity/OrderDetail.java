package java6.assgiment.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted;

    public BigDecimal getSubTotal() {
        return price.multiply(new BigDecimal(quantity));
    }

    // ✅ Thêm phương thức để lấy ảnh sản phẩm
    public String getProductPhoto() {
        return product != null ? product.getPhoto() : null;
    }

    // ✅ Thêm phương thức để lấy tên sản phẩm (tuỳ chọn)
    public String getProductName() {
        return product != null ? product.getNameProduct() : null;
    }
}

package java6.assgiment.DAO;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.Product;

public interface ProductDAO extends JpaRepository<Product, Long> {
    // Tìm kiếm theo classify và tên sản phẩm (không phân biệt hoa thường)
    Page<Product> findByClassifyAndNameProductContainingIgnoreCase(String classify, String nameProduct, Pageable pageable);

    // Lọc theo classify
    Page<Product> findByClassify(String classify, Pageable pageable);

    // Tìm kiếm theo tên sản phẩm (không phân biệt hoa thường)
    Page<Product> findByNameProductContainingIgnoreCase(String nameProduct, Pageable pageable);

    Optional<Product> findById(Integer id);

    List<Product> findByIsDeletedFalse();
}

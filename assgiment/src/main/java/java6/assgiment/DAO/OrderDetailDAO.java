package java6.assgiment.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.OrderDetail;

public interface OrderDetailDAO extends JpaRepository<OrderDetail, Long> {
}

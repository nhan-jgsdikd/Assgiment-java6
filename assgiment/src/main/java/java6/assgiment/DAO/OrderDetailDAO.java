package java6.assgiment.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.OrderDetail;
import java6.assgiment.Entity.Orders;

public interface OrderDetailDAO extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrderAndIsDeleted(Orders cartOrder, boolean b);
}

package java6.assgiment.DAO;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.Orders;
import java6.assgiment.Entity.Orders.OrderStatus;
import java6.assgiment.Entity.User;

public interface OrderDAO extends JpaRepository<Orders, Long> {

    Optional<Orders> findByUserAndStatusAndIsDeleted(User loggedInUser, OrderStatus preparing, boolean b);

    List<Orders> findByUserAndIsDeleted(User loggedInUser, boolean b);

    List<Orders> findByStatusAndIsDeleted(OrderStatus preparing, boolean b);

    List<Orders> findByStatus(OrderStatus preparing);

    List<Orders> findByUser(User loggedInUser);

    List<Orders> findByUserAndStatusAndIsDeletedFalse(User loggedInUser, OrderStatus preparing);
}
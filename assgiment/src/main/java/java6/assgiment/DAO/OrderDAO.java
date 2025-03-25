package java6.assgiment.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.Orders;

public interface OrderDAO extends JpaRepository<Orders, Long> {
}
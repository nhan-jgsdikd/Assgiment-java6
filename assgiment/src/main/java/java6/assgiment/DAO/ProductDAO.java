package java6.assgiment.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.Product;

public interface ProductDAO extends JpaRepository<Product, Long> {

}

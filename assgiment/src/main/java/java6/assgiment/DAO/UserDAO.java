package java6.assgiment.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.User;

public interface UserDAO extends JpaRepository<User, Long> {

    User findByEmail(String email);

}

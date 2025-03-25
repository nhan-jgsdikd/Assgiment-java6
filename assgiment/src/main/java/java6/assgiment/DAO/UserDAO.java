package java6.assgiment.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.User;
import java6.assgiment.Entity.User.Role;

public interface UserDAO extends JpaRepository<User, Long> {

    User findByEmail(String email);
    boolean existsByEmail(String email);
    User findByUsername(String username);
    List<User> findByRole(String string);
    List<User> findByRoleAndIsDeleted(Role user, boolean b);
}

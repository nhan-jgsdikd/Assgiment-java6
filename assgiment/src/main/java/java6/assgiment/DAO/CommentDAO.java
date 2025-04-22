package java6.assgiment.DAO;


import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.Comment;

import java.util.List;

public interface CommentDAO extends JpaRepository<Comment, Long> {
    List<Comment> findByProductId(Long productId);
}
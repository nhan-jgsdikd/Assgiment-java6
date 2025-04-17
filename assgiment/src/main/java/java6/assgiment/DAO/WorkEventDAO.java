package java6.assgiment.DAO;



import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.WorkEvent;



public interface WorkEventDAO extends JpaRepository<WorkEvent, Long> {
    List<WorkEvent> findByIsDeletedFalseAndStartDateAfter(LocalDateTime date);
    List<WorkEvent> findByIsDeletedFalseAndStartDateBefore(LocalDateTime date);
    List<WorkEvent> findByIsDeletedFalse();
}
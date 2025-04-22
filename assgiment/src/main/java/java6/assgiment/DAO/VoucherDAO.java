package java6.assgiment.DAO;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import java6.assgiment.Entity.Voucher;

public interface VoucherDAO extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
}
package java6.assgiment.Controller.SecurityConfig;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.User;
import java6.assgiment.Entity.User.Role;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDAO userDAO;

    public CustomUserDetailsService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    @Transactional // Đảm bảo lấy dữ liệu đầy đủ từ Hibernate
    public UserDetails loadUserByUsername(String username) {
        Optional<User> optionalUser = Optional.ofNullable(userDAO.findByEmail(username));

        User user = optionalUser.orElseThrow(() -> 
            new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + username));

        // Kiểm tra trạng thái isDeleted
        if (user.getIsDeleted()) {
            throw new UsernameNotFoundException("Tài khoản với email " + username + " đã bị xóa.");
        }

        // Lấy role từ enum và thêm tiền tố "ROLE_" nếu cần
        String role = "ROLE_" + user.getRole().name();
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            authorities
        );
    }
}
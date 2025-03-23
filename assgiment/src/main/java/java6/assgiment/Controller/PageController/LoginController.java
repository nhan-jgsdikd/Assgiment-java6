package java6.assgiment.Controller.PageController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.User;

@Controller
public class LoginController {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               Model model,
                               HttpSession session) {
        try {
            // Xác thực người dùng với Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            // Đặt authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Tìm user từ cơ sở dữ liệu để lưu vào session
            User user = userDAO.findByEmail(email);
            if (user == null) {
                model.addAttribute("error", "Email không tồn tại!");
                return "login/login";
            }

            // Lưu thông tin người dùng vào session
            session.setAttribute("loggedInUser", user);

            // System.out.println("Authorities: " + authentication.getAuthorities());

            // Chuyển hướng dựa trên vai trò
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/dashboard";
            } else {
                return "redirect:/";
            }

        } catch (AuthenticationException e) {
            // Xử lý khi xác thực thất bại (email hoặc mật khẩu sai)
            model.addAttribute("error", "Email hoặc mật khẩu không đúng!");
            return "login/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa session người dùng
        SecurityContextHolder.clearContext(); // Xóa context bảo mật
        return "redirect:/login";
    }
}
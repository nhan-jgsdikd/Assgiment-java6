package java6.assgiment.Controller.PageController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.User;

@Controller
public class LoginController {

    @Autowired
    private UserDAO userDAO;

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
            // Kiểm tra xem user có tồn tại không
            User user = userDAO.findByEmail(email);
            if (user == null) {
                model.addAttribute("error", "Email không tồn tại!");
                return "login/login";
            }

            // Xác thực người dùng với Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            // Đặt authentication vào SecurityContext
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Lưu SecurityContext vào session để duy trì trạng thái đăng nhập
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

            // Lưu thông tin user vào session
            session.setAttribute("loggedInUser", user);

            System.out.println("Authorities: " + authentication.getAuthorities());

            // Chuyển hướng dựa trên vai trò
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
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

    // API kiểm tra thông tin người dùng sau khi đăng nhập
    @GetMapping("/user-info")
    @ResponseBody
    public Authentication getUserInfo() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}

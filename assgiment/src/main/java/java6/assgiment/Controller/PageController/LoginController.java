package java6.assgiment.Controller.PageController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            User user = userDAO.findByEmail(email);
            if (user == null) {
                model.addAttribute("error", "Email không tồn tại!");
                return "login/login";
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
            session.setAttribute("loggedInUser", user);

            return getRedirectUrl(authentication);
        } catch (AuthenticationException e) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng!");
            return "login/login";
        }
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "login/signup";
    }

    @PostMapping("/signup")
    public String processSignup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            HttpSession session) {
        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu không khớp");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "signup/signup";
        }

        // Check if email already exists
        if (userDAO.findByEmail(email) != null) {
            model.addAttribute("error", "Email đã được sử dụng");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "signup/signup";
        }

        // Create and save new user
        User newUser = new User();
        newUser.setUsername(name);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(User.Role.USER); // Default role for new users
        userDAO.save(newUser);

        // Authenticate the new user
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
            session.setAttribute("loggedInUser", newUser);

            return getRedirectUrl(authentication);
        } catch (AuthenticationException e) {
            model.addAttribute("error", "Đăng ký thành công nhưng đăng nhập thất bại. Vui lòng thử đăng nhập lại.");
            return "login/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @GetMapping("/user-info")
    @ResponseBody
    public Authentication getUserInfo() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String getRedirectUrl(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/dashboard";
        } else {
            return "redirect:/";
        }
    }
}
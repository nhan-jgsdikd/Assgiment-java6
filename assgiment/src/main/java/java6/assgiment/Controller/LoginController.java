package java6.assgiment.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import java6.assgiment.DAO.UserDAO;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java6.assgiment.Entity.User;


@Controller
public class LoginController {

    @Autowired
    private UserDAO userDAO;


    
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "login/login";
    }

        @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                              @RequestParam String password,
                              Model model,
                              HttpSession session) {
        User user = userDAO.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "Email không tồn tại!");
            return "login/login";
        }

        if (!password.equals(user.getPassword())) {
            model.addAttribute("error", "Mật khẩu không chính xác!");
            return "login/login";
        }

        session.setAttribute("loggedInUser", user);

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/";
        } else {
            return "redirect:/";
        }
    }
    
    @GetMapping("/signup")
    public String signup(Model model) {
        return "login/signup";
    }

    @PostMapping("/signup")
    public String processSignup(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               Model model) {
        if (userDAO.findByEmail(email) != null) {
            model.addAttribute("error", "Email đã được sử dụng!");
            return "login/signup";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "login/signup";
        }

        User newUser = new User();
        newUser.setUsername(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole("USER");
        userDAO.save(newUser);

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
}

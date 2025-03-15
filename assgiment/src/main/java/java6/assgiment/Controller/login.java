package java6.assgiment.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class login {
    
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "login/login";
    }

    
    @GetMapping("/signup")
    public String signup(Model model) {
        return "login/signup";
    }
    
}

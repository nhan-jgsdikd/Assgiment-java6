package java6.assgiment.Controller.AdminController;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.OrderDAO;
import java6.assgiment.DAO.ProductDAO;
import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.User;
@Controller
public class AdminController {

    @Autowired
    private HttpSession session;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ProductDAO productDAO;

    
    @Autowired
    private OrderDAO ordersDAO;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        model.addAttribute("user", user);
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);
        return "Admin/Dashboard";
    }

    

















    
}
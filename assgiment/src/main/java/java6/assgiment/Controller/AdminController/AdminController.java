package java6.assgiment.Controller.AdminController;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.User;
import java6.assgiment.DAO.ProductDAO;
import java6.assgiment.Entity.Product;

@Controller
public class AdminController {

    @Autowired
    private HttpSession session;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ProductDAO productDAO;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        model.addAttribute("user", user);
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);
        return "Admin/Dashboard";
    }

    




    @GetMapping("/Oder")
    public String Oder(Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);





        return "Admin/Oder";
    }




    @GetMapping("/Collaborate")
    public String Collaborate(Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);






        return "Admin/Collaborate";
    }




    @GetMapping("/Feedback")
    public String Feedback(Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);






        return "Admin/Feedback";
    }

    
}
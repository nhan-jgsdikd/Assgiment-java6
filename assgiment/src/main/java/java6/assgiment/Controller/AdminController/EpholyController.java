package java6.assgiment.Controller.AdminController;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.User;

public class EpholyController {
    



    @Autowired
    private HttpSession session;

    @Autowired
    private UserDAO userDAO;


        
    @GetMapping("/Staff")
    public String Staff(Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);
        model.addAttribute("Allaccount", userDAO.findAll());


        return "Admin/Staff";
    }


}

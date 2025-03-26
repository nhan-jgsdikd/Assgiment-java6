package java6.assgiment.Controller.PageController;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.OrderDAO;
import java6.assgiment.Entity.Orders;
import java6.assgiment.Entity.User;

@Controller
public class ProfileController {

    @Autowired
    private HttpSession session;

    @Autowired
    private OrderDAO ordersDAO;

    @GetMapping("/profile")
    public String profile(Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Lấy danh sách đơn hàng của người dùng (chưa bị xóa)
        List<Orders> orders = ordersDAO.findByUserAndIsDeleted(loggedInUser, false);

        model.addAttribute("user", loggedInUser);
        model.addAttribute("orders", orders);
        return "Dashboard/profile";
    }
}
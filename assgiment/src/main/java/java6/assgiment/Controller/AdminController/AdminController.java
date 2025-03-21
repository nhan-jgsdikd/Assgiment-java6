package java6.assgiment.Controller.AdminController;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.User;

@Controller
public class AdminController {

    @Autowired
    private HttpSession session;

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Lấy user từ session
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            // Nếu user null, chuyển hướng về trang đăng nhập
            return "redirect:/login";
        }
        // Thêm user vào model để sử dụng trong template
        model.addAttribute("user", user);
        // Lấy danh sách users và thêm vào model
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);
        return "Admin/Dashboard";
    }

    @GetMapping("/Staff")
    public String Staff() {
        return "Admin/Staff"; // Đảm bảo tên này khớp với file trong thư mục templates
    }
    

    @GetMapping("/Products")
    public String Products() {
        return "Admin/Products";
    }

    @GetMapping("/Oder")
    public String Oder() {
        return "Admin/Oder";
    }

    @GetMapping("/Collaborate")
    public String Collaborate() {
        return "Admin/Collaborate";
    }

    @GetMapping("/Feedback")
    public String Feedback() {
        return "Admin/Feedback";
    }

    
}
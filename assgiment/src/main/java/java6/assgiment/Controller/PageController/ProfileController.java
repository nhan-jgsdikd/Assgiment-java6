package java6.assgiment.Controller.PageController;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.OrderDAO;
import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.Orders;
import java6.assgiment.Entity.User;

@Controller
public class ProfileController {

    @Autowired
    private HttpSession session;

    @Autowired
    private OrderDAO ordersDAO;

    @Autowired
    private UserDAO userDAO;

    private static final String UPLOAD_DIR = "F:" + File.separator + "Githup" + File.separator + "Assgiment" 
                            + File.separator + "Assgiment-java6" + File.separator + "assgiment" 
                            + File.separator + "src" + File.separator + "main" + File.separator + "resources" 
                            + File.separator + "static" + File.separator + "img";

    @GetMapping("/profileoder")
    public String profileOrder(Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        List<Orders> orders = ordersDAO.findByUser(loggedInUser);
        model.addAttribute("user", loggedInUser);
        model.addAttribute("orders", orders);
        return "Dashboard/Profile/profileoder";
    }

    @GetMapping("/profileaccount")
    public String profileAccount(Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loggedInUser);
        return "Dashboard/Profile/profileaccount";
    }

    @GetMapping("/profileddress")
    public String profileAddress(Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loggedInUser);
        return "Dashboard/Profile/profileddress";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        List<Orders> orders = ordersDAO.findByUser(loggedInUser);
        List<Orders> recentOrders = orders.stream()
            .sorted(Comparator.comparing(Orders::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(3)
            .collect(Collectors.toList());
        model.addAttribute("user", loggedInUser);
        model.addAttribute("orders", recentOrders);
        return "Dashboard/Profile/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam(value = "imgFile", required = false) MultipartFile imgFile,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            RedirectAttributes redirectAttributes) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            loggedInUser.setUsername(username);
            loggedInUser.setEmail(email);
            loggedInUser.setPhone(phone);

            if (imgFile != null && !imgFile.isEmpty()) {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String fileName = System.currentTimeMillis() + "_" + imgFile.getOriginalFilename();
                File destination = new File(uploadDir, fileName);
                imgFile.transferTo(destination);
                loggedInUser.setImg("/img/" + fileName);
            }

            userDAO.save(loggedInUser);
            session.setAttribute("loggedInUser", loggedInUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi upload ảnh: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/update-address")
    public String updateAddress(
            @RequestParam(value = "addressLine", required = false) String addressLine,
            @RequestParam(value = "ward", required = false) String ward,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "city", required = false) String city,
            RedirectAttributes redirectAttributes) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            // Cập nhật các trường địa chỉ
            if (addressLine != null) loggedInUser.setAddressLine(addressLine);
            if (ward != null) loggedInUser.setWard(ward);
            if (district != null) loggedInUser.setDistrict(district);
            if (city != null) loggedInUser.setCity(city);

            userDAO.save(loggedInUser);
            session.setAttribute("loggedInUser", loggedInUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/profileddress"; // Chuyển hướng về trang địa chỉ sau khi cập nhật
    }
<<<<<<< HEAD






    @GetMapping("/caimewgiv")
    public String caimewgiv(Model model) {

        return "Dashboard/Profile/caimewgiv";
    }





=======
>>>>>>> 5b7f43e002990d06e1b784d451983dd72f0b2a31
}
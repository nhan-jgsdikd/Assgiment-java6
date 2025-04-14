package java6.assgiment.Controller.PageController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.OrderDAO;
import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.Orders;
import java6.assgiment.Entity.User;
import java6.assgiment.Entity.Orders.OrderStatus;

@Controller
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

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

        // Lấy danh sách đơn hàng của người dùng
        List<Orders> orders = ordersDAO.findByUser(loggedInUser);

        // Phân loại đơn hàng theo trạng thái
        Map<OrderStatus, List<Orders>> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Orders::getStatus));

        // Đảm bảo tất cả trạng thái đều có trong model
        model.addAttribute("preparingOrders", ordersByStatus.getOrDefault(OrderStatus.PREPARING, List.of()));
        model.addAttribute("shippingOrders", ordersByStatus.getOrDefault(OrderStatus.SHIPPING, List.of()));
        model.addAttribute("deliveredOrders", ordersByStatus.getOrDefault(OrderStatus.DELIVERED, List.of()));
        model.addAttribute("cancelledOrders", ordersByStatus.getOrDefault(OrderStatus.CANCELLED, List.of()));
        model.addAttribute("user", loggedInUser);
        model.addAttribute("hasOrders", !orders.isEmpty()); // Thêm hasOrders

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

        // Lấy danh sách đơn hàng của người dùng
        List<Orders> orders = ordersDAO.findByUser(loggedInUser);

        // Phân loại đơn hàng theo trạng thái
        Map<OrderStatus, List<Orders>> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Orders::getStatus));

        // Đảm bảo tất cả trạng thái đều có trong model
        model.addAttribute("preparingOrders", ordersByStatus.getOrDefault(OrderStatus.PREPARING, List.of()));
        model.addAttribute("shippingOrders", ordersByStatus.getOrDefault(OrderStatus.SHIPPING, List.of()));
        model.addAttribute("deliveredOrders", ordersByStatus.getOrDefault(OrderStatus.DELIVERED, List.of()));
        model.addAttribute("cancelledOrders", ordersByStatus.getOrDefault(OrderStatus.CANCELLED, List.of()));
        model.addAttribute("user", loggedInUser);
        model.addAttribute("hasOrders", !orders.isEmpty()); // Thêm hasOrders

        return "Dashboard/Profile/profile";
    }

    @GetMapping("/order/cancel/{id}")
    public String showCancelOrderForm(@PathVariable("id") Long orderId, Model model, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thực hiện thao tác này!");
            return "redirect:/login";
        }

        Orders order = ordersDAO.findById(orderId).orElse(null);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại!");
            return "redirect:/profileoder";
        }

        // Kiểm tra xem đơn hàng có thuộc về người dùng hiện tại không
        if (!order.getUser().getId().equals(loggedInUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy đơn hàng này!");
            return "redirect:/profileoder";
        }

        // Kiểm tra trạng thái đơn hàng (chỉ cho phép hủy nếu đang ở trạng thái PREPARING)
        if (order.getStatus() != OrderStatus.PREPARING) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không thể hủy ở trạng thái hiện tại!");
            return "redirect:/profileoder";
        }

        model.addAttribute("order", order);
        return "Dashboard/Profile/cancel_reason_form"; // New template for cancellation reason form
    }

    @PostMapping("/order/cancel/{id}")
    public String cancelOrder(
            @PathVariable("id") Long orderId,
            @RequestParam("cancelReason") String cancelReason,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thực hiện thao tác này!");
            return "redirect:/login";
        }

        try {
            // Tìm đơn hàng theo ID
            Orders order = ordersDAO.findById(orderId).orElse(null);
            if (order == null) {
                logger.warn("Order with ID {} not found", orderId);
                redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại!");
                return "redirect:/profileoder";
            }

            // Kiểm tra xem đơn hàng có thuộc về người dùng hiện tại không
            if (!order.getUser().getId().equals(loggedInUser.getId())) {
                logger.warn("User {} attempted to cancel order {} which they do not own", loggedInUser.getId(), orderId);
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy đơn hàng này!");
                return "redirect:/profileoder";
            }

            // Kiểm tra trạng thái đơn hàng (chỉ cho phép hủy nếu đang ở trạng thái PREPARING)
            if (order.getStatus() != OrderStatus.PREPARING) {
                logger.warn("Order {} cannot be canceled because its status is {}", orderId, order.getStatus());
                redirectAttributes.addFlashAttribute("error", "Đơn hàng không thể hủy ở trạng thái hiện tại!");
                return "redirect:/profileoder";
            }

            // Cập nhật trạng thái đơn hàng thành CANCELLED và lưu lý do hủy
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelReason(cancelReason);
            ordersDAO.save(order);
            logger.info("Order {} canceled successfully by user {} with reason: {}", orderId, loggedInUser.getId(), cancelReason);

            redirectAttributes.addFlashAttribute("success", "Hủy đơn hàng thành công!");
        } catch (Exception e) {
            logger.error("Error canceling order {}: {}", orderId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi hủy đơn hàng: " + e.getMessage());
        }

        return "redirect:/profileoder";
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
        return "redirect:/profileddress";
    }

    @GetMapping("/caimewgiv")
    public String caimewgiv(Model model) {
        return "Dashboard/Profile/caimewgiv";
    }
}
package java6.assgiment.Controller.AdminController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.OrderDAO;
import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.Orders;
import java6.assgiment.Entity.User;
import java6.assgiment.Entity.Orders.OrderStatus;

@Controller
public class OrdersAdminController {

    @Autowired
    private HttpSession session;

    @Autowired
    private OrderDAO ordersDAO;

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/Oder")
    public String listOrders(Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Lấy tất cả đơn hàng
        List<Orders> allOrders = ordersDAO.findAll();

        // Phân loại đơn hàng theo trạng thái
        List<Orders> preparingOrders = allOrders.stream()
            .filter(order -> order.getStatus() == OrderStatus.PREPARING)
            .collect(Collectors.toList());
        List<Orders> shippingOrders = allOrders.stream()
            .filter(order -> order.getStatus() == OrderStatus.SHIPPING)
            .collect(Collectors.toList());
        List<Orders> deliveredOrders = allOrders.stream()
            .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        List<Orders> cancelledOrders = allOrders.stream()
            .filter(order -> order.getStatus() == OrderStatus.CANCELLED) // Thêm trạng thái CANCELLED
            .collect(Collectors.toList());

        // Thêm vào model
        model.addAttribute("preparingOrders", preparingOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);

        return "Admin/Oder";
    }
}
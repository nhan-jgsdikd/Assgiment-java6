package java6.assgiment.Controller.AdminController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        
        List<Orders> allOrders = ordersDAO.findAll();

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
            .filter(order -> order.getStatus() == OrderStatus.CANCELLED)
            .collect(Collectors.toList());

        // Thêm vào model
        model.addAttribute("preparingOrders", preparingOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);

        return "Admin/Oder";
    }

    @PostMapping("/orders/process")
    public String processOrders(
            @RequestParam("orderIds") List<Long> orderIds,
            @RequestParam("action") String action,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (orderIds == null || orderIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một đơn hàng để xử lý!");
            return "redirect:/Oder";
        }

        List<Orders> ordersToProcess = ordersDAO.findAllById(orderIds);

        if ("approve".equals(action)) {
            // Duyệt đơn: PREPARING -> SHIPPING
            for (Orders order : ordersToProcess) {
                if (order.getStatus() == OrderStatus.PREPARING) {
                    if (order.getShippingAddressLine() == null || order.getShippingAddressLine().trim().isEmpty()) {
                        redirectAttributes.addFlashAttribute("error", "Không thể duyệt đơn hàng #" + order.getId() + " vì thiếu địa chỉ giao!");
                        return "redirect:/Oder";
                    }
                    order.setStatus(OrderStatus.SHIPPING);
                    ordersDAO.save(order);
                }
            }
            redirectAttributes.addFlashAttribute("success", "Đã duyệt " + ordersToProcess.size() + " đơn hàng thành công!");
        } else if ("cancel".equals(action)) {
            // Hủy đơn: PREPARING -> CANCELLED
            for (Orders order : ordersToProcess) {
                if (order.getStatus() == OrderStatus.PREPARING) {
                    order.setStatus(OrderStatus.CANCELLED);
                    ordersDAO.save(order);
                }
            }
            redirectAttributes.addFlashAttribute("success", "Đã hủy " + ordersToProcess.size() + " đơn hàng thành công!");
        }

        return "redirect:/Oder";
    }

    @GetMapping("/AllOder")
    public String AllOder(Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        List<Orders> allOrders = ordersDAO.findAll();

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
            .filter(order -> order.getStatus() == OrderStatus.CANCELLED)
            .collect(Collectors.toList());

        // Thêm vào model
        model.addAttribute("preparingOrders", preparingOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);

        return "Admin/fragments/AllOrder";
    }
}
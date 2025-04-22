package java6.assgiment.Controller.Shipper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java6.assgiment.DAO.OrderDAO;
import java6.assgiment.Entity.Orders;
import java6.assgiment.Entity.Orders.OrderStatus;

@Controller
public class HomeController {

    @Autowired
    private OrderDAO ordersDAO;

    @GetMapping("/homeshipper")
    public String home(Model model) {
        // Lấy tất cả đơn hàng và lọc SHIPPING, chưa bị xóa
        List<Orders> allOrders = ordersDAO.findAll();
        List<Orders> shippingOrders = allOrders.stream()
            .filter(order -> order.getStatus() == OrderStatus.SHIPPING)
            .collect(Collectors.toList());

        model.addAttribute("shippingOrders", shippingOrders);


        return "Shipper/home";
    }

    @PostMapping("/homeshipper/deliver")
    public String deliver(@RequestParam(value = "orderIds", required = false) List<Long> orderIds,
                         RedirectAttributes redirectAttributes) {
        if (orderIds != null && !orderIds.isEmpty()) {
            try {
                for (Long orderId : orderIds) {
                    Orders order = ordersDAO.findById(orderId).orElse(null);
                    if (order != null && order.getStatus() == OrderStatus.SHIPPING) {
                        order.setStatus(OrderStatus.DELIVERED);
                        ordersDAO.save(order);
                    }
                }
                redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng thành công!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật trạng thái đơn hàng!");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một đơn hàng!");
        }
        
        return "redirect:/homeshipper";
    }
}
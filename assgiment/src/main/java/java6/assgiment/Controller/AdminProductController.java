package java6.assgiment.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminProductController {

    // Xử lý yêu cầu hiển thị trang quản lý sản phẩm
    @GetMapping("/admin/products")
    public String showProductManagementPage() {
        return "Admin/Editproductst"; // Trả về tên file HTML (Adminproduct.html)
    }
}
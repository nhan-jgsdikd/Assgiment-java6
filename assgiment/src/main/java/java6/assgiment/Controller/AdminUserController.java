
package java6.assgiment.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminUserController {

    // Xử lý yêu cầu hiển thị trang quản lý sản phẩm
    @GetMapping("/admin/User")
    public String showUserManagementPage() {
        return "Admin/Edituser"; // Trả về tên file HTML (Adminproduct.html)
    }

}
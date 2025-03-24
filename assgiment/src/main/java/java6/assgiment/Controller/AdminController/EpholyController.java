package java6.assgiment.Controller.AdminController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.UserDAO;
import java6.assgiment.Entity.User;

@Controller
public class EpholyController { // Renamed from EpholyController for clarity

    @Autowired
    private HttpSession session;

    @Autowired
    private UserDAO userDAO;

    private static final String UPLOAD_DIR = "F:\\Githup\\Assgiment\\Assgiment-java6\\assgiment\\src\\main\\resources\\static\\img";

    // Display the staff management page
    @GetMapping("/Staff")
    public String staffManagement(Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        List<User> users = userDAO.findByRole("USER");
        users.forEach(u -> {
            if (u.getImg() == null || u.getImg().isEmpty()) {
                u.setImg("/img/default-avatar.jpg");
            }
        });
        model.addAttribute("user", currentUser);
        model.addAttribute("users", users);
        return "Admin/Staff";
    }

    // Show form for adding a new user
    @GetMapping("/edit-user")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        return "Admin/fragments/UserFormNew";
    }

    // Show form for editing an existing user
    @GetMapping("/edit-user/{id}")
    public String showUserForm(@PathVariable("id") Optional<Long> id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = id.map(userDAO::findById).orElse(Optional.empty());
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            return "Admin/fragments/UserFormNew";
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên có ID: " + id.orElse(null));
            return "redirect:/Staff";
        }
    }

    // Save user (create or update)
    @PostMapping("/save-user")
    public String saveUser(
        @ModelAttribute("user") User user,
        @RequestParam(value = "avatarFile", required = false) MultipartFile file,
        @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (user.getId() == null) {
                // Thêm mới nhân viên
                if (user.getPassword() == null || user.getPassword().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu là bắt buộc!");
                    return "redirect:/edit-user";
                }
                if (!user.getPassword().equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                    return "redirect:/edit-user";
                }
            } else {
                // Cập nhật nhân viên
                Optional<User> existingUserOpt = userDAO.findById(user.getId());
                if (existingUserOpt.isPresent()) {
                    User existingUser = existingUserOpt.get();
                    // Nếu không có mật khẩu mới, giữ nguyên mật khẩu cũ
                    if (user.getPassword() == null || user.getPassword().isEmpty()) {
                        user.setPassword(existingUser.getPassword());
                    } else {
                        // Nếu có mật khẩu mới, kiểm tra xác nhận
                        if (!user.getPassword().equals(confirmPassword)) {
                            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                            return "redirect:/edit-user/" + user.getId();
                        }
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên để cập nhật.");
                    return "redirect:/Staff";
                }
            }
    
            // Xử lý upload ảnh
            if (file != null && !file.isEmpty()) {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();
    
                String originalFileName = file.getOriginalFilename();
                String fileName = originalFileName;
                File destination = new File(uploadDir, fileName);
    
                // Kiểm tra xem file đã tồn tại chưa, nếu có thì tạo tên mới
                int suffix = 1;
                while (destination.exists()) {
                    String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
                    String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
                    fileName = baseName + "_" + suffix + extension;
                    destination = new File(uploadDir, fileName);
                    suffix++;
                }
    
                file.transferTo(destination);
                user.setImg("/img/" + fileName); // Lưu tên file đã điều chỉnh vào cơ sở dữ liệu
            } else if (user.getId() == null && (user.getImg() == null || user.getImg().isEmpty())) {
                user.setImg("/img/default-avatar.jpg");
            }
    
            user.setRole("USER");
            userDAO.save(user);
            redirectAttributes.addFlashAttribute("success", 
                user.getId() != null ? "Cập nhật thành công!" : "Thêm mới thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
            return "redirect:/Staff";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/Staff";
        }
        return "redirect:/Staff";
    }

    // Delete user
    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userDAO.findById(id);
        if (userOpt.isPresent()) {
            userDAO.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên để xóa.");
        }
        return "redirect:/Staff";
    }
}
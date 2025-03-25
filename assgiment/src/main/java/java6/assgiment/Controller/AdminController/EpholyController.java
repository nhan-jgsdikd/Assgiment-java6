package java6.assgiment.Controller.AdminController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java6.assgiment.Entity.User.Role;

@Controller
public class EpholyController {

    @Autowired
    private HttpSession session;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder; // Thêm để mã hóa mật khẩu

    private static final String UPLOAD_DIR = "F:\\Githup\\Assgiment\\Assgiment-java6\\assgiment\\src\\main\\resources\\static\\img";

    // Display the staff management page
    @GetMapping("/Staff")
    public String staffManagement(Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        // Lấy danh sách user chưa bị xóa với role = USER
        List<User> users = userDAO.findByRoleAndIsDeleted(Role.USER, false);
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
        if (userOpt.isPresent() && !userOpt.get().getIsDeleted()) {
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
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu là bắt buộc khi tạo mới!");
                    return "redirect:/edit-user";
                }
                if (!user.getPassword().equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                    return "redirect:/edit-user";
                }
                // Mã hóa mật khẩu khi tạo mới
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                // Cập nhật nhân viên
                Optional<User> existingUserOpt = userDAO.findById(user.getId());
                if (existingUserOpt.isPresent() && !existingUserOpt.get().getIsDeleted()) {
                    User existingUser = existingUserOpt.get();
                    // Nếu không nhập mật khẩu mới, giữ nguyên mật khẩu cũ
                    if (user.getPassword() == null || user.getPassword().isEmpty()) {
                        user.setPassword(existingUser.getPassword());
                    } else {
                        // Nếu có mật khẩu mới, kiểm tra xác nhận và mã hóa
                        if (!user.getPassword().equals(confirmPassword)) {
                            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                            return "redirect:/edit-user/" + user.getId();
                        }
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
                String fileName = System.currentTimeMillis() + "_" + originalFileName; // Tạo tên file duy nhất
                File destination = new File(uploadDir, fileName);

                file.transferTo(destination);
                user.setImg("/img/" + fileName);
            } else if (user.getId() == null && (user.getImg() == null || user.getImg().isEmpty())) {
                user.setImg("/img/default-avatar.jpg");
            }

            // Đặt role là USER và isDeleted là false
            user.setRole(Role.USER);
            user.setIsDeleted(false);
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

    // Delete user (soft delete)
    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userDAO.findById(id);
        if (userOpt.isPresent() && !userOpt.get().getIsDeleted()) {
            User user = userOpt.get();
            user.setIsDeleted(true); // Xóa mềm
            userDAO.save(user);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên để xóa.");
        }
        return "redirect:/Staff";
    }
}
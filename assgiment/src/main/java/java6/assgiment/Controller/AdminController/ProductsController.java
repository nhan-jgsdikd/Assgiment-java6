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
import java6.assgiment.DAO.ProductDAO;
import java6.assgiment.Entity.Product;

@Controller
public class ProductsController {

    @Autowired
    private ProductDAO productDAO;

    // Đường dẫn lưu trữ ảnh sản phẩm
    private static final String PHOTO_UPLOAD_DIR = "F:\\Githup\\Assgiment\\Assgiment-java6\\assgiment\\src\\main\\resources\\static\\img";

    // Hiển thị danh sách sản phẩm
    @GetMapping("/Products")
    public String product(Model model) {
        List<Product> products = productDAO.findAll();
        products.forEach(p -> {
            if (p.getPhoto() == null || p.getPhoto().isEmpty()) {
                p.setPhoto("/images/default-product.jpg");
            }
        });
        model.addAttribute("products", products);
        return "Admin/Products";
    }

    @GetMapping("/edit-product")
public String showAddForm(Model model) {
    model.addAttribute("product", new Product());
    return "Admin/fragments/ProductFormNew";
}

    // Hiển thị form chỉnh sửa sản phẩm
    @GetMapping("/edit-product/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productDAO.findById(id);
        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            return "Admin/fragments/ProductFormNew"; // Cần đặt đúng tên file template
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm có ID: " + id);
            return "redirect:/Products";
        }
    }

    // Lưu sản phẩm (tạo mới hoặc cập nhật)
    @PostMapping("/save-product")
    public String saveProduct(@ModelAttribute("product") Product product,
                             @RequestParam("photoFile") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            if (file != null && !file.isEmpty()) {
                File uploadDir = new File(PHOTO_UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                File destination = new File(uploadDir, fileName);
                file.transferTo(destination);
                product.setPhoto("/img/" + fileName);
            } else if (product.getPhoto() == null || product.getPhoto().isEmpty()) {
                product.setPhoto("/images/default-product.jpg");
            }

            productDAO.save(product);
            redirectAttributes.addFlashAttribute("success", "Lưu sản phẩm thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
            return "redirect:/Products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/Products";
        }
        return "redirect:/Products";
    }

    // Xóa sản phẩm
    @PostMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productDAO.findById(id);
        if (productOpt.isPresent()) {
            productDAO.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm để xóa.");
        }
        return "redirect:/Products";
    }
}

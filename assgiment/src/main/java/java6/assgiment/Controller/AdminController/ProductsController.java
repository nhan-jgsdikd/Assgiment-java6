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
        // Chỉ lấy sản phẩm chưa bị xóa
        List<Product> products = productDAO.findByIsDeletedFalse();
        products.forEach(p -> {
            if (p.getPhoto() == null || p.getPhoto().isEmpty()) {
                p.setPhoto("/img/default-product.jpg"); // Sửa đường dẫn cho đồng bộ
            }
        });
        model.addAttribute("products", products);
        return "Admin/Products";
    }

    // Hiển thị form thêm mới sản phẩm
    @GetMapping("/edit-product")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "Admin/fragments/ProductFormNew";
    }

    // Hiển thị form chỉnh sửa sản phẩm
    @GetMapping("/edit-product/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productDAO.findById(id);
        if (productOpt.isPresent() && !productOpt.get().getIsDeleted()) {
            model.addAttribute("product", productOpt.get());
            return "Admin/fragments/ProductFormNew";
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm có ID: " + id);
            return "redirect:/Products";
        }
    }

    // Lưu sản phẩm (tạo mới hoặc cập nhật)
    @PostMapping("/save-product")
    public String saveProduct(
        @ModelAttribute("product") Product product,
        @RequestParam(value = "photoFile", required = false) MultipartFile file,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Kiểm tra dữ liệu đầu vào
            if (product.getNameProduct() == null || product.getNameProduct().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tên sản phẩm là bắt buộc!");
                return product.getId() == null ? "redirect:/edit-product" : "redirect:/edit-product/" + product.getId();
            }
            if (product.getPrice() == null || product.getPrice() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Giá sản phẩm phải lớn hơn 0!");
                return product.getId() == null ? "redirect:/edit-product" : "redirect:/edit-product/" + product.getId();
            }
            if (product.getQuanlity() == null || product.getQuanlity() < 0) {
                redirectAttributes.addFlashAttribute("error", "Số lượng tồn kho không được âm!");
                return product.getId() == null ? "redirect:/edit-product" : "redirect:/edit-product/" + product.getId();
            }

            // Xử lý upload ảnh
            if (file != null && !file.isEmpty()) {
                File uploadDir = new File(PHOTO_UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                File destination = new File(uploadDir, fileName);
                file.transferTo(destination);
                product.setPhoto("/img/" + fileName);
            } else if (product.getId() == null && (product.getPhoto() == null || product.getPhoto().isEmpty())) {
                product.setPhoto("/img/default-product.jpg");
            } else if (product.getId() != null) {
                // Giữ nguyên ảnh cũ nếu không upload ảnh mới
                Optional<Product> existingProduct = productDAO.findById(product.getId());
                if (existingProduct.isPresent()) {
                    product.setPhoto(existingProduct.get().getPhoto());
                }
            }

            // Đảm bảo isDeleted là false khi tạo mới hoặc cập nhật
            product.setIsDeleted(false);
            productDAO.save(product);
            redirectAttributes.addFlashAttribute("success", 
                product.getId() != null ? "Cập nhật sản phẩm thành công!" : "Thêm sản phẩm thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
            return "redirect:/Products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/Products";
        }
        return "redirect:/Products";
    }

    // Xóa sản phẩm (xóa mềm)
    @PostMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productDAO.findById(id);
        if (productOpt.isPresent() && !productOpt.get().getIsDeleted()) {
            Product product = productOpt.get();
            product.setIsDeleted(true); // Xóa mềm
            productDAO.save(product);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm để xóa.");
        }
        return "redirect:/Products";
    }
}
package java6.assgiment.Controller.PageController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.ProductDAO;
import java6.assgiment.Entity.Product;
import java6.assgiment.Stass.CartItem;

@Controller
public class ProductController {

    @Autowired
    private ProductDAO productDAO;

    @GetMapping("/product")
    public String product(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "default") String sort,
            @RequestParam(name = "classify", required = false, defaultValue = "") String classify,
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            Model model) {

        // Xác định cách sắp xếp dựa trên giá trị của "sort"
        Pageable pageable;
        switch (sort) {
            case "price-asc":
                pageable = PageRequest.of(page, size, Sort.by("price").ascending());
                break;
            case "price-desc":
                pageable = PageRequest.of(page, size, Sort.by("price").descending());
                break;
            case "newest":
                pageable = PageRequest.of(page, size, Sort.by("id").descending());
                break;
            default:
                pageable = PageRequest.of(page, size);
                break;
        }

        // Tìm kiếm và lọc sản phẩm
        Page<Product> productPage;
        if (!classify.isEmpty() && !search.isEmpty()) {
            productPage = productDAO.findByClassifyAndNameProductContainingIgnoreCase(classify, search, pageable);
        } else if (!classify.isEmpty()) {
            productPage = productDAO.findByClassify(classify, pageable);
        } else if (!search.isEmpty()) {
            productPage = productDAO.findByNameProductContainingIgnoreCase(search, pageable);
        } else {
            productPage = productDAO.findAll(pageable);
        }

        // Thêm các thuộc tính vào model để sử dụng trong Thymeleaf
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedClassify", classify);
        model.addAttribute("searchQuery", search);

        // Trả về tên view tương ứng với HTML
        return "Dashboard/Products";
    }

    @PostMapping("/add-to-cart")
    public String addToCart(
            @RequestParam("productId") Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "default") String sort,
            @RequestParam(defaultValue = "") String classify,
            @RequestParam(defaultValue = "") String search,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (session.getAttribute("loggedInUser") == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
            return "redirect:/login";
        }

        // Tìm sản phẩm theo ID
        Optional<Product> productOptional = productDAO.findById(productId);
        if (!productOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại");
            return "redirect:/product";
        }

        // Lấy hoặc khởi tạo giỏ hàng từ session
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        // Thêm sản phẩm vào giỏ hàng
        Product product = productOptional.get();
        boolean productExists = false;
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + 1);
                productExists = true;
                break;
            }
        }

        if (!productExists) {
            cart.add(new CartItem(product, 1));
        }

        // Thêm các tham số để giữ trạng thái trang sau khi redirect
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("classify", classify);
        redirectAttributes.addAttribute("search", search);

        redirectAttributes.addFlashAttribute("message", "Đã thêm sản phẩm vào giỏ hàng!");
        return "redirect:/product";
    }
}
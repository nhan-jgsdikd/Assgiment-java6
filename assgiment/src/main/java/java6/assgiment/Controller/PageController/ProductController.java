package java6.assgiment.Controller.PageController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java6.assgiment.DAO.CommentDAO;
import java6.assgiment.DAO.ProductDAO;
import java6.assgiment.Entity.Comment;
import java6.assgiment.Entity.Product;


import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CommentDAO commentRepository;

    @GetMapping("/product")
    public String product(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "default") String sort,
            @RequestParam(name = "classify", required = false, defaultValue = "") String classify,
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            Model model) {

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

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedClassify", classify);
        model.addAttribute("searchQuery", search);

        return "Dashboard/Products";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        // Tìm sản phẩm theo ID
        Product product = productDAO.findById(id).orElse(null);

        // Nếu không tìm thấy sản phẩm, chuyển hướng về trang danh sách
        if (product == null) {
            return "redirect:/product";
        }

        // Lấy danh sách bình luận của sản phẩm
        List<Comment> comments = commentRepository.findByProductId(id);

        // Thêm sản phẩm và bình luận vào model để truyền sang view
        model.addAttribute("product", product);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());

        // Trả về tên view cho trang chi tiết sản phẩm
        return "Dashboard/ProductDetail";
    }

    @PostMapping("/product/comment")
    public String addComment(
            @RequestParam("productId") Long productId,
            @RequestParam("content") String content,
            Model model) {
        // Tìm sản phẩm theo ID
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Lấy username từ người dùng đăng nhập
        String username = "Guest"; // Mặc định nếu không đăng nhập
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        }

        // Tạo bình luận mới với username từ người dùng
        Comment comment = Comment.builder()
                .username(username)
                .content(content)
                .createdAt(LocalDateTime.now())
                .product(product)
                .build();

        // Lưu bình luận vào database
        commentRepository.save(comment);

        // Chuyển hướng về trang chi tiết sản phẩm
        return "redirect:/product/" + productId;
    }
}
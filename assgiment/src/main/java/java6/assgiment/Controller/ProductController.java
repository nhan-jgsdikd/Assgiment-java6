package java6.assgiment.Controller;

import org.hibernate.mapping.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java6.assgiment.DAO.ProductDAO;
import java6.assgiment.Entity.Product;
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


        // Xử lý sắp xếp
        Pageable pageable;
        switch (sort) {
            case "price-asc":
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "price"));
                break;
            case "price-desc":
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "price"));
                break;
            case "newest":
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
                break;
            default:
                // Không sắp xếp, hiển thị tất cả sản phẩm theo thứ tự mặc định
                pageable = PageRequest.of(page, size);
                break;

            
        }


        // Xử lý lọc danh mục và tìm kiếm
        Page<Product> productPage;
        if (!classify.isEmpty() && !search.isEmpty()) {
            // Lọc theo classify và tìm kiếm
            productPage = productDAO.findByClassifyAndNameProductContainingIgnoreCase(classify, search, pageable);
        } else if (!classify.isEmpty()) {
            // Lọc theo classify
            productPage = productDAO.findByClassify(classify, pageable);
        } else if (!search.isEmpty()) {
            // Tìm kiếm theo tên sản phẩm
            productPage = productDAO.findByNameProductContainingIgnoreCase(search, pageable);
        } else {
            // Không lọc, không tìm kiếm
            productPage = productDAO.findAll(pageable);
        }

        // Truyền dữ liệu vào model
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedClassify", classify);
        model.addAttribute("searchQuery", search);

        return "Dashboard/Products"; 
    }
}
package java6.assgiment.Controller.PageController;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.BannerDAO;
import java6.assgiment.DAO.ProductDAO;
import java6.assgiment.Entity.Banner;
import java6.assgiment.Entity.Product;

@Controller
public class IndexController {

    @Autowired
    HttpSession session;

    @Autowired
    ServletContext context;

    @Autowired
    ProductDAO productDAO;

    @Autowired
    BannerDAO bannerDAO; // Đổi tên biến cho đúng quy tắc

    @GetMapping("/")
    public String home(Model model) {
        // Lấy tất cả banner
        List<Banner> bannerProducts = bannerDAO.findAll(); 
        model.addAttribute("bannerProducts", bannerProducts);
        
        // Lấy 5 sản phẩm nổi bật
        List<Product> products = productDAO.findAll();
        List<Product> limitedProducts = products.stream().limit(5).collect(Collectors.toList());
        model.addAttribute("indexstaff", limitedProducts);
        
        return "Dashboard/index"; 
    }

}

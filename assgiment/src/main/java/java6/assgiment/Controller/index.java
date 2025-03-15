package java6.assgiment.Controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.ProductDAO;
import java6.assgiment.Entity.Product;

@Controller
public class index {

    @Autowired
    HttpSession session;
    
    @Autowired 
    ServletContext context;

    @Autowired
    ProductDAO productDAO; // Nên đổi tên thành productDAO (viết thường chữ đầu)

    @GetMapping("/")
    public String home(Model model) {
        List<Product> products = productDAO.findAll(); 
        List<Product> limitedProducts = products.stream().limit(6).toList();
        model.addAttribute("indexstaff", limitedProducts); 
        return "Dashboard/index"; 
    }
    
    @GetMapping("/product")
    public String Product(Model model) {
        List<Product> products = productDAO.findAll(); 
        model.addAttribute("products", products); 
        return "Dashboard/Products"; 
    }

    @GetMapping("/cart")
    public String Cart(Model model) {
        return "Dashboard/Cart";
    }
}
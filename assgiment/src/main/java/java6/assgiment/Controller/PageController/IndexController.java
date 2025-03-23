package java6.assgiment.Controller.PageController;

import java.util.Arrays;
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
    BannerDAO bannerDAO;

    @GetMapping("/")
    public String home(Model model) {

        List<Banner> bannerProducts = bannerDAO.findAll(); 
        model.addAttribute("bannerProducts", bannerProducts);
        
        List<Product> products = productDAO.findAll();
        List<Product> limitedProducts = products.stream().limit(8).collect(Collectors.toList());
        model.addAttribute("indexstaff", limitedProducts);

        List<Long> productIds = Arrays.asList(1L, 9L, 14L, 15L, 30L, 35L, 46L, 50L);
        List<Product> specificProducts = productDAO.findAllById(productIds);
        model.addAttribute("specificProducts", specificProducts);

        List<Long> productIDS = Arrays.asList(11L, 12L, 13L, 14L, 15L, 26L, 27L, 28L, 29L, 30L);
        List<Product> perfumeproducts = productDAO.findAllById(productIDS);
        model.addAttribute("perfumeproducts", perfumeproducts);


        
        return "Dashboard/index"; 
    }

}

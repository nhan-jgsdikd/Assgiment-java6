package java6.assgiment.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.ProductDAO;


@Controller
public class CartController {

    @Autowired
    HttpSession session;
    
    @Autowired 
    ServletContext context;

    @Autowired
    ProductDAO productDAO; 

    
 

    @GetMapping("/cart")
    public String Cart(Model model) {
        return "Dashboard/Cart";
    }
}
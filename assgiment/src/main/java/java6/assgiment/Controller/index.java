package java6.assgiment.Controller;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import ch.qos.logback.core.model.Model;

@Controller
public class index {

    @GetMapping("/")
    public String showIndex(Model model) {
        return "Dashboard/index";
    }
    
    @GetMapping("/product")
    public String Product(Model model) {
        return "Dashboard/Products";
    }

    @GetMapping("/cart")
    public String Cart(Model model) {
        return "Dashboard/Cart";
    }
    
}

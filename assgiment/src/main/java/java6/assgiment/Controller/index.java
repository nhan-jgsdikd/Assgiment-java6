package java6.assgiment.Controller;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import ch.qos.logback.core.model.Model;

@Controller
public class index {

    @GetMapping("/")
    public String index(Model model) {
        return "Dashboard/index";
    }
    
}

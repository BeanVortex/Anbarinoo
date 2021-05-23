package ir.darkdeveloper.anbarinoo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class MainController {
    
    @GetMapping("/")
    public String landingPage(){
        return "index.html";
    }

}

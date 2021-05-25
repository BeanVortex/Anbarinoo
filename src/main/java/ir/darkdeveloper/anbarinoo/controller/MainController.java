package ir.darkdeveloper.anbarinoo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    
    @GetMapping("/")
    public String landingPage(){
        return "/static/index";
    }
}

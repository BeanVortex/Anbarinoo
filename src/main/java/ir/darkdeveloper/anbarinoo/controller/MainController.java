package ir.darkdeveloper.anbarinoo.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class MainController {
    
    @GetMapping("/")
    public String landingPage(){
        return "index.html";
    }
    @GetMapping("/info")
    @ResponseBody
    public Principal p(Principal principal){
        return principal;
    }

    @GetMapping("/forbidden")
    @ResponseBody
    public String forbidden(){
        return "Error 403: Forbidden";
    }
    

}

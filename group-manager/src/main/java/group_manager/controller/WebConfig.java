package group_manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebConfig {

    @RequestMapping(value = {
            "/duties", "/cleaning", "/stats",
            "/duties/**", "/cleaning/**", "/stats/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}

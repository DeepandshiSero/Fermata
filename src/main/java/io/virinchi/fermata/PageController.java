package io.virinchi.fermata;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/home", "/Home.html"})
    public String home() {
        return "Home";
    }

    @GetMapping("/about")
    public String about() {
        return "Aboutus";
    }

    @GetMapping("/details")
    public String details() {
        return "Details";
    }

    @GetMapping("/details2")
    public String details2() {
        return "Details2";
    }
    @GetMapping("/payment")
    public String payment() {
        return "Payment";
    }
}
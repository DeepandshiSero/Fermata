package io.virinchi.fermata.controller;

import io.virinchi.fermata.model.User;
import io.virinchi.fermata.service.PianoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PianoController {

    private final PianoService pianoService;

    public PianoController(PianoService pianoService) {
        this.pianoService = pianoService;
    }

    @GetMapping("/piano")
    public String pianoPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("isLoggedIn", user != null);
        model.addAttribute("pianoEnrolled", user != null && user.isPianoEnrolled());
        return "Piano";
    }

    @PostMapping("/piano/enroll")
    public String enroll(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        pianoService.enrollUserInPiano(user);
        session.setAttribute("loggedInUser", user);
        return "redirect:/piano";
    }
}
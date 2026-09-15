package io.virinchi.fermata.controller;

import io.virinchi.fermata.model.User;
import io.virinchi.fermata.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String admin(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            return "redirect:/";
        }

        List<User> allUsers = adminService.getAllUsers();

        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("users", allUsers);

        return "Admin";
    }

    @PostMapping("/admin/piano/remove")
    public String removeFromPiano(@RequestParam Long userId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!"ROLE_ADMIN".equals(currentUser.getRole())) {
            return "redirect:/";
        }

        adminService.removeUserFromPiano(userId);
        return "redirect:/admin";
    }
}
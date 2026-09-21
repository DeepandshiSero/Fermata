package io.virinchi.fermata.controller;

import io.virinchi.fermata.model.Order;
import io.virinchi.fermata.model.User;
import io.virinchi.fermata.service.AdminService;
import io.virinchi.fermata.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    public AdminController(AdminService adminService, OrderService orderService) {
        this.adminService = adminService;
        this.orderService = orderService;
    }

    private boolean isAdmin(HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        return currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole());
    }

    @GetMapping("/admin")
    public String admin(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        if (!isAdmin(session)) {
            return "redirect:/";
        }

        List<User> allUsers = adminService.getAllUsers();
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("users", allUsers);

        return "Admin";
    }

    @GetMapping("/admin/user/{id}")
    public String userDetail(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        if (!isAdmin(session)) {
            return "redirect:/";
        }

        return adminService.getUserById(id).map(targetUser -> {
            List<Order> userOrders = orderService.getOrdersForUser(targetUser);

            model.addAttribute("targetUser", targetUser);
            model.addAttribute("orders", userOrders);
            model.addAttribute("statusOptions", List.of("Pending", "Confirmed", "Shipped", "Delivered", "Cancelled"));

            return "AdminUserDetail";
        }).orElse("redirect:/admin");
    }

    @PostMapping("/admin/piano/remove")
    public String removeFromPiano(@RequestParam Long userId, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        adminService.removeUserFromPiano(userId);
        return "redirect:/admin/user/" + userId;
    }

    @PostMapping("/admin/order/status")
    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam String status,
                                    @RequestParam Long userId, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        orderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/user/" + userId;
    }

    @PostMapping("/admin/order/delete")
    public String deleteOrder(@RequestParam Long orderId, @RequestParam Long userId, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        orderService.deleteOrder(orderId);
        return "redirect:/admin/user/" + userId;
    }
}
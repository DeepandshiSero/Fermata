package io.virinchi.fermata.controller;

import io.virinchi.fermata.model.CartItem;
import io.virinchi.fermata.model.User;
import io.virinchi.fermata.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        cartService.removeFromCart(id, user);
        return "redirect:/checkout";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItem> cart = cartService.getCartForUser(user);
        double total = cartService.getCartTotal(user);

        model.addAttribute("cartItems", cart);
        model.addAttribute("cartTotal", total);
        return "Checkout";
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<String> addToCart(
            @RequestParam String id,
            @RequestParam String name,
            @RequestParam double price,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam String imageUrl,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(401).body("You must be signed in to make purchases");
        }

        cartService.addToCart(user, id, name, price, quantity, imageUrl);
        return ResponseEntity.ok().build();
    }
}
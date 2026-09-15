package io.virinchi.fermata.service;

import io.virinchi.fermata.model.CartItem;
import io.virinchi.fermata.model.User;
import io.virinchi.fermata.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public List<CartItem> getCartForUser(User user) {
        return cartRepository.findByUser(user);
    }

    public void addToCart(User user, String productId, String name, double price, int quantity, String imageUrl) {
        Optional<CartItem> existing = cartRepository.findByUserAndProductId(user, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartRepository.save(item);
        } else {
            cartRepository.save(new CartItem(productId, name, price, quantity, imageUrl, user));
        }
    }

    public void removeFromCart(Long cartItemId, User user) {
        cartRepository.findById(cartItemId).ifPresent(item -> {
            if (item.getUser().getId().equals(user.getId())) {
                cartRepository.deleteById(cartItemId);
            }
        });
    }

    public double getCartTotal(User user) {
        return getCartForUser(user).stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
package io.virinchi.fermata.service;

import io.virinchi.fermata.model.CartItem;
import io.virinchi.fermata.model.Order;
import io.virinchi.fermata.model.OrderItem;
import io.virinchi.fermata.model.User;
import io.virinchi.fermata.repository.CartRepository;
import io.virinchi.fermata.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public Order placeOrder(User user) {
        List<CartItem> cartItems = cartRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus("Pending");

        double total = 0;
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(order, cartItem.getName(), cartItem.getPrice(), cartItem.getQuantity());
            order.getItems().add(orderItem);
            total += cartItem.getPrice() * cartItem.getQuantity();
        }
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteByUser(user);

        return savedOrder;
    }

    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUserOrderByPlacedAtDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByPlacedAtDesc();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(newStatus);
            orderRepository.save(order);
        });
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }
}
package io.virinchi.fermata.service;

import io.virinchi.fermata.model.Order;
import io.virinchi.fermata.model.User;
import io.virinchi.fermata.repository.OrderRepository;
import io.virinchi.fermata.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AdminService(
            UserRepository userRepository,
            OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void removeUserFromPiano(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPianoEnrolled(false);
            userRepository.save(user);
        });
    }

    @Transactional
    public void deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        List<Order> userOrders =
                orderRepository.findByUserOrderByPlacedAtDesc(user);

        if (!userOrders.isEmpty()) {
            orderRepository.deleteAll(userOrders);
            orderRepository.flush();
        }

        userRepository.delete(user);
    }
}
package com.example.order.controller;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.UpdateStatusRequest;
import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.example.order.security.JwtPrincipal;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@Valid @RequestBody CreateOrderRequest request, Authentication authentication) {
        return orderService.create(request, principal(authentication).userId());
    }
    @GetMapping public List<Order> all(Authentication authentication) {
        if (!principal(authentication).role().equals("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Only administrators can view all orders");
        }
        return orderService.all();
    }
    @GetMapping("/{id}") public Order get(@PathVariable Long id, Authentication authentication) {
        Order order = orderService.get(id);
        assertOwnerOrAdmin(order, principal(authentication));
        return order;
    }
    @GetMapping("/user/{userId}") public List<Order> byUser(@PathVariable Long userId, Authentication authentication) {
        JwtPrincipal principal = principal(authentication);
        if (!principal.role().equals("ADMIN") && !principal.userId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Cannot view another user's orders");
        }
        return orderService.byUser(userId);
    }
    @PutMapping("/{id}/status") public Order updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) { return orderService.updateStatus(id, request.status()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { orderService.delete(id); }

    private JwtPrincipal principal(Authentication authentication) {
        return (JwtPrincipal) authentication.getPrincipal();
    }

    private void assertOwnerOrAdmin(Order order, JwtPrincipal principal) {
        if (!principal.role().equals("ADMIN") && !principal.userId().equals(order.getUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Cannot view another user's order");
        }
    }
}

package com.example.order.service;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.entity.Order;
import com.example.order.entity.OrderStatus;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.exception.StockUnavailableException;
import com.example.order.exception.MedicineNotFoundException;
import com.example.order.exception.MedicineServiceUnavailableException;
import com.example.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestClient medicineClient;
    private final String internalServiceToken;

    public OrderService(OrderRepository orderRepository, RestClient medicineClient,
                        @Value("${internal.service-token:}") String internalServiceToken) {
        this.orderRepository = orderRepository;
        this.medicineClient = medicineClient;
        this.internalServiceToken = internalServiceToken;
    }

    @Transactional
    public Order create(CreateOrderRequest request, Long authenticatedUserId) {
        MedicineSnapshot medicine;
        try {
                medicine = medicineClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/medicines/{id}/stock/reduce").queryParam("quantity", request.quantity()).build(request.medicineId()))
                        .header("X-Internal-Service-Token", internalServiceToken)
                    .retrieve().body(MedicineSnapshot.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new MedicineNotFoundException("Medicine not found");
            }
            if (exception.getStatusCode().value() == 409) {
                throw new StockUnavailableException("Insufficient stock");
            }
            throw new MedicineServiceUnavailableException("Medicine service rejected the order");
        } catch (RestClientException exception) {
            throw new MedicineServiceUnavailableException("Medicine service is unavailable");
        }
        if (medicine == null) throw new StockUnavailableException("Medicine service returned no medicine");
        Order order = new Order();
        order.setUserId(authenticatedUserId);
        order.setMedicineId(request.medicineId());
        order.setQuantity(request.quantity());
        order.setTotalPrice(BigDecimal.valueOf(medicine.price()).multiply(BigDecimal.valueOf(request.quantity())));
        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    public List<Order> all() { return orderRepository.findAll(); }
    public List<Order> byUser(Long userId) { return orderRepository.findByUserId(userId); }
    public Order get(Long id) { return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found")); }
    public Order updateStatus(Long id, OrderStatus status) { Order order = get(id); order.setStatus(status); return orderRepository.save(order); }
    public void delete(Long id) { orderRepository.delete(get(id)); }

    public record MedicineSnapshot(Long id, double price, int stock) {}
}

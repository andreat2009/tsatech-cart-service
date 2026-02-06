package com.newproject.cart.service;

import com.newproject.cart.domain.Cart;
import com.newproject.cart.dto.CartRequest;
import com.newproject.cart.dto.CartResponse;
import com.newproject.cart.events.EventPublisher;
import com.newproject.cart.exception.NotFoundException;
import com.newproject.cart.repository.CartRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final EventPublisher eventPublisher;

    public CartService(CartRepository cartRepository, EventPublisher eventPublisher) {
        this.cartRepository = cartRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CartResponse create(CartRequest request) {
        Cart cart = new Cart();
        cart.setCustomerId(request.getCustomerId());
        cart.setCurrency(request.getCurrency());
        cart.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        OffsetDateTime now = OffsetDateTime.now();
        cart.setCreatedAt(now);
        cart.setUpdatedAt(now);

        Cart saved = cartRepository.save(cart);
        eventPublisher.publish("CART_CREATED", "cart", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public CartResponse update(Long id, CartRequest request) {
        Cart cart = cartRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Cart not found"));

        cart.setCurrency(request.getCurrency());
        cart.setStatus(request.getStatus() != null ? request.getStatus() : cart.getStatus());
        cart.setUpdatedAt(OffsetDateTime.now());

        Cart saved = cartRepository.save(cart);
        eventPublisher.publish("CART_UPDATED", "cart", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CartResponse get(Long id) {
        Cart cart = cartRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Cart not found"));
        return toResponse(cart);
    }

    @Transactional(readOnly = true)
    public List<CartResponse> list(Long customerId) {
        if (customerId != null) {
            return cartRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        }
        return cartRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        Cart cart = cartRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Cart not found"));
        cartRepository.delete(cart);
        eventPublisher.publish("CART_DELETED", "cart", id.toString(), null);
    }

    private CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setCustomerId(cart.getCustomerId());
        response.setCurrency(cart.getCurrency());
        response.setStatus(cart.getStatus());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        return response;
    }
}

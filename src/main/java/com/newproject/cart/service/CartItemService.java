package com.newproject.cart.service;

import com.newproject.cart.domain.Cart;
import com.newproject.cart.domain.CartItem;
import com.newproject.cart.dto.CartItemRequest;
import com.newproject.cart.dto.CartItemResponse;
import com.newproject.cart.events.EventPublisher;
import com.newproject.cart.exception.NotFoundException;
import com.newproject.cart.repository.CartItemRepository;
import com.newproject.cart.repository.CartRepository;
import com.newproject.cart.security.RequestActor;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartItemService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final EventPublisher eventPublisher;
    private final RequestActor requestActor;

    public CartItemService(
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        EventPublisher eventPublisher,
        RequestActor requestActor
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.eventPublisher = eventPublisher;
        this.requestActor = requestActor;
    }

    @Transactional
    public CartItemResponse addItem(Long cartId, CartItemRequest request) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new NotFoundException("Cart not found"));
        requestActor.assertCustomerAccessIfAuthenticated(cart.getCustomerId());

        CartItem item = cartItemRepository.findByCartIdAndProductId(cartId, request.getProductId())
            .orElseGet(CartItem::new);

        boolean created = item.getId() == null;
        if (created) {
            item.setCart(cart);
            item.setProductId(request.getProductId());
            item.setQuantity(request.getQuantity());
            item.setUnitPrice(request.getUnitPrice());
        } else {
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setUnitPrice(request.getUnitPrice());
        }

        CartItem saved = cartItemRepository.save(item);
        eventPublisher.publish(created ? "CART_ITEM_ADDED" : "CART_ITEM_UPDATED", "cart_item", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public CartItemResponse updateItem(Long itemId, CartItemRequest request) {
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Cart item not found"));
        requestActor.assertCustomerAccessIfAuthenticated(item.getCart().getCustomerId());

        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());

        CartItem saved = cartItemRepository.save(item);
        eventPublisher.publish("CART_ITEM_UPDATED", "cart_item", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public CartItemResponse updateQuantity(Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Cart item not found"));
        requestActor.assertCustomerAccessIfAuthenticated(item.getCart().getCustomerId());

        item.setQuantity(quantity);
        CartItem saved = cartItemRepository.save(item);
        eventPublisher.publish("CART_ITEM_UPDATED", "cart_item", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> listItems(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new NotFoundException("Cart not found"));
        requestActor.assertCustomerAccessIfAuthenticated(cart.getCustomerId());
        return cartItemRepository.findByCartId(cartId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void removeItem(Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Cart item not found"));
        requestActor.assertCustomerAccessIfAuthenticated(item.getCart().getCustomerId());
        cartItemRepository.delete(item);
        eventPublisher.publish("CART_ITEM_REMOVED", "cart_item", itemId.toString(), null);
    }

    private CartItemResponse toResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setCartId(item.getCart().getId());
        response.setProductId(item.getProductId());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        return response;
    }
}

package com.newproject.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.newproject.cart.domain.Cart;
import com.newproject.cart.domain.CartItem;
import com.newproject.cart.dto.CartItemRequest;
import com.newproject.cart.dto.CartItemResponse;
import com.newproject.cart.events.EventPublisher;
import com.newproject.cart.repository.CartItemRepository;
import com.newproject.cart.repository.CartRepository;
import com.newproject.cart.security.RequestActor;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartItemServiceVariantTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private RequestActor requestActor;

    private CartItemService cartItemService;

    @BeforeEach
    void setUp() {
        cartItemService = new CartItemService(cartRepository, cartItemRepository, eventPublisher, requestActor);
    }

    @Test
    void addItemMergesQuantityOnlyWithinSameVariantScope() {
        Cart cart = new Cart();
        cart.setId(10L);
        cart.setCustomerId(7L);

        CartItem existing = new CartItem();
        existing.setId(99L);
        existing.setCart(cart);
        existing.setProductId(1008L);
        existing.setVariantKey("RED-M");
        existing.setVariantDisplayName("Rosso / M");
        existing.setQuantity(1);
        existing.setUnitPrice(new BigDecimal("12.00"));

        CartItemRequest request = new CartItemRequest();
        request.setProductId(1008L);
        request.setVariantKey("RED-M");
        request.setVariantDisplayName("Rosso / M");
        request.setQuantity(2);
        request.setUnitPrice(new BigDecimal("12.00"));

        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductIdAndVariantKey(10L, 1008L, "RED-M")).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemResponse response = cartItemService.addItem(10L, request);

        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getVariantKey()).isEqualTo("RED-M");
        assertThat(response.getVariantDisplayName()).isEqualTo("Rosso / M");
        verify(eventPublisher).publish(org.mockito.ArgumentMatchers.eq("CART_ITEM_UPDATED"), org.mockito.ArgumentMatchers.eq("cart_item"), org.mockito.ArgumentMatchers.eq("99"), any());
    }
}

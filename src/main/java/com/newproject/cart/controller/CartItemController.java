package com.newproject.cart.controller;

import com.newproject.cart.dto.CartItemRequest;
import com.newproject.cart.dto.CartItemResponse;
import com.newproject.cart.service.CartItemService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartItemController {
    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping("/{cartId}/items")
    public List<CartItemResponse> list(@PathVariable Long cartId) {
        return cartItemService.listItems(cartId);
    }

    @PostMapping("/{cartId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse add(@PathVariable Long cartId, @Valid @RequestBody CartItemRequest request) {
        return cartItemService.addItem(cartId, request);
    }

    @PutMapping("/items/{itemId}")
    public CartItemResponse update(@PathVariable Long itemId, @Valid @RequestBody CartItemRequest request) {
        return cartItemService.updateItem(itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long itemId) {
        cartItemService.removeItem(itemId);
    }
}

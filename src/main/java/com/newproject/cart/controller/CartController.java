package com.newproject.cart.controller;

import com.newproject.cart.dto.CartRequest;
import com.newproject.cart.dto.CartResponse;
import com.newproject.cart.service.CartService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartResponse> list(@RequestParam(value = "customerId", required = false) Long customerId) {
        return cartService.list(customerId);
    }

    @GetMapping("/{id}")
    public CartResponse get(@PathVariable Long id) {
        return cartService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse create(@Valid @RequestBody CartRequest request) {
        return cartService.create(request);
    }

    @PutMapping("/{id}")
    public CartResponse update(@PathVariable Long id, @Valid @RequestBody CartRequest request) {
        return cartService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cartService.delete(id);
    }
}

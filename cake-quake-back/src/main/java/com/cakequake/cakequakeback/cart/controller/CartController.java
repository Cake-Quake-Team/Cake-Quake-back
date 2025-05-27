package com.cakequake.cakequakeback.cart.controller;

import com.cakequake.cakequakeback.cart.dto.AddCart;
import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/buyers")
public class CartController {
    private final CartService cartService;

//    @PostMapping("/{userId}/cart")
//    public ResponseEntity<AddCart> addCart(@PathVariable("userId") long userId,
//                                           @RequestBody @Valid AddCart addCart) {
//
//
//    }
//
//    @GetMapping("")
//    public ResponseEntity<List<Cart>> getCarts() {
//
//    }
//
//    @PatchMapping("")
//    public ResponseEntity<Cart> updateCart(@RequestBody Cart cart) {
//
//    }
//
//    @DeleteMapping("")
//    public ResponseEntity<Cart> deleteCart(@RequestBody Cart cart) {
//
//    }

}

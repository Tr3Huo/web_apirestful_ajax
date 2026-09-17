package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products-ajax")
public class ProductUIController {
    
    @GetMapping
    public String index() {
        return "products/ajax";
    }
}

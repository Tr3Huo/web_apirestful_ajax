package com.example.demo.controllers.api;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.entity.Product;
import com.example.demo.entity.Category;
import com.example.demo.model.Response;
import com.example.demo.repository.ProductRepository;
import com.example.demo.services.ICategoryService;
import com.example.demo.services.IStorageService;

@RestController
@RequestMapping(path = "/api/product")
public class ProductAPIController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ICategoryService categoryService;

    @Autowired
    IStorageService storageService;

    @GetMapping
    public ResponseEntity<?> getAllProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String keyword) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;
        
        if (keyword.isEmpty()) {
            productPage = productRepository.findAll(pageable);
        } else {
            productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        
        return new ResponseEntity<Response>(new Response(true, "Thành công", productPage), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable("id") Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return new ResponseEntity<Response>(new Response(true, "Thành công", product.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<Response>(new Response(false, "Thất bại", null), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/addProduct")
    public ResponseEntity<?> addProduct(
            @RequestParam("name") String name,
            @RequestParam("price") java.math.BigDecimal price,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        Product product = new Product();
        if(image != null && !image.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            String uuString = uuid.toString();
            product.setImages(storageService.getSorageFilename(image, uuString));
            storageService.store(image, product.getImages());
        }
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setDescription(description);
        
        Optional<Category> optCategory = categoryService.findById(categoryId);
        optCategory.ifPresent(product::setCategory);
        
        Product saved = productRepository.save(product);
        return new ResponseEntity<Response>(new Response(true, "Thêm Thành công", saved), HttpStatus.OK);
    }

    @PutMapping(path = "/updateProduct/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("price") java.math.BigDecimal price,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image) {
            
        Optional<Product> optProduct = productRepository.findById(id);
        if (optProduct.isEmpty()) {
            return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Product", null), HttpStatus.BAD_REQUEST);
        } else {
            Product product = optProduct.get();
            if(image != null && !image.isEmpty()) {
                UUID uuid = UUID.randomUUID();
                String uuString = uuid.toString();
                product.setImages(storageService.getSorageFilename(image, uuString));
                storageService.store(image, product.getImages());
            }
            product.setName(name);
            product.setPrice(price);
            product.setQuantity(quantity);
            product.setDescription(description);
            
            Optional<Category> optCategory = categoryService.findById(categoryId);
            optCategory.ifPresent(product::setCategory);
            
            Product saved = productRepository.save(product);
            return new ResponseEntity<Response>(new Response(true, "Cập nhật Thành công", saved), HttpStatus.OK);
        }
    }

    @DeleteMapping(path = "/deleteProduct/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") Long id){
        Optional<Product> optProduct = productRepository.findById(id);
        if (optProduct.isEmpty()) {
            return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Product", null), HttpStatus.BAD_REQUEST);
        } else {
            productRepository.deleteById(id);
            return new ResponseEntity<Response>(new Response(true, "Xóa Thành công", optProduct.get()), HttpStatus.OK);
        }
    }
}

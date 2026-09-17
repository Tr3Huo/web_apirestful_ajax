package com.example.demo.controllers.api;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.entity.Category;
import com.example.demo.model.Response;
import com.example.demo.services.ICategoryService;
import com.example.demo.services.IStorageService;

@RestController
@RequestMapping(path = "/api/category")
public class CategoryAPIController {
    
    @Autowired
    private ICategoryService categoryService;

    @Autowired
    IStorageService storageService;

    @GetMapping
    public ResponseEntity<?> getAllCategory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String keyword) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Category> categoryPage;
        
        if (keyword.isEmpty()) {
            categoryPage = categoryService.findAll(pageable);
        } else {
            categoryPage = categoryService.findByCategoryNameContaining(keyword, pageable);
        }
        
        return new ResponseEntity<Response>(new Response(true, "Thành công", categoryPage), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCategoryList() {
        return new ResponseEntity<Response>(new Response(true, "Thành công", categoryService.findAll()), HttpStatus.OK);
    }

    @PostMapping(path = "/getCategory")
    public ResponseEntity<?> getCategory(@Validated @RequestParam("id") Long id) {
        Optional<Category> category = categoryService.findById(id);
        if (category.isPresent()) {
            return new ResponseEntity<Response>(new Response(true, "Thành công", category.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<Response>(new Response(false, "Thất bại", null), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/addCategory")
    public ResponseEntity<?> addCategory(@Validated @RequestParam("categoryName") String categoryName,
            @Validated @RequestParam("icon") MultipartFile icon) {
        Optional<Category> optCategory = categoryService.findByCategoryName(categoryName);
        if (optCategory.isPresent()) {
            return new ResponseEntity<Response>(new Response(false, "Category đã tồn tại trong hệ thống", null), HttpStatus.BAD_REQUEST);
        } else {
            Category category = new Category();
            if(!icon.isEmpty()) {
                UUID uuid = UUID.randomUUID();
                String uuString = uuid.toString();
                category.setIcon(storageService.getSorageFilename(icon, uuString));
                storageService.store(icon, category.getIcon());
            }
            category.setCategoryName(categoryName);
            categoryService.save(category);
            return new ResponseEntity<Response>(new Response(true, "Thêm Thành công", category), HttpStatus.OK);
        }
    }

    @PutMapping(path = "/updateCategory")
    public ResponseEntity<?> updateCategory(@Validated @RequestParam("categoryId") Long categoryId,
            @Validated @RequestParam("categoryName") String categoryName,
            @Validated @RequestParam("icon") MultipartFile icon) {
        Optional<Category> optCategory = categoryService.findById(categoryId);
        if (optCategory.isEmpty()) {
            return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Category", null), HttpStatus.BAD_REQUEST);
        } else if(optCategory.isPresent()) {
            if(!icon.isEmpty()) {
                UUID uuid = UUID.randomUUID();
                String uuString = uuid.toString();
                optCategory.get().setIcon(storageService.getSorageFilename(icon, uuString));
                storageService.store(icon, optCategory.get().getIcon());
            }
            optCategory.get().setCategoryName(categoryName);
            categoryService.save(optCategory.get());
            return new ResponseEntity<Response>(new Response(true, "Cập nhật Thành công", optCategory.get()), HttpStatus.OK);
        }
        return null;
    }

    @DeleteMapping(path = "/deleteCategory")
    public ResponseEntity<?> deleteCategory(@Validated @RequestParam("categoryId") Long categoryId){
        Optional<Category> optCategory = categoryService.findById(categoryId);
        if (optCategory.isEmpty()) {
            return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Category", null), HttpStatus.BAD_REQUEST);
        } else if(optCategory.isPresent()) {
            categoryService.delete(optCategory.get());
            return new ResponseEntity<Response>(new Response(true, "Xóa Thành công", optCategory.get()), HttpStatus.OK);
        }
        return null;
    }
}

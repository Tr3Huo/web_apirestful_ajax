package com.example.demo.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Product;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.repository.ProductRepository;
import com.example.demo.services.ProductService;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final Path uploadDir = Paths.get("D:/Study/NAM3/KY1/DOT1/WEB/Workspace/MVCSpringBoot/btth07/saveimages");

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> products;
        
        if (keyword == null || keyword.isBlank()) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        }
        return products.map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        return productMapper.toDTO(product);
    }

    @Override
    public ProductDTO create(ProductDTO dto) {
        try {
            String fileName = saveImage(dto.getImage());
            dto.setImages(fileName);
            Product product = productMapper.toEntity(dto);
            Product saved = productRepository.save(product);
            return productMapper.toDTO(saved);
        } catch (IOException e) {
            throw new RuntimeException("Không thể upload ảnh", e);
        }
    }

    @Override
    public ProductDTO update(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        try {
            MultipartFile image = dto.getImage();
            if (image != null && !image.isEmpty()) {
                String oldImage = product.getImages();
                String newImage = saveImage(image);
                dto.setImages(newImage);
                deleteImage(oldImage);
            } else {
                dto.setImages(product.getImages());
            }
            productMapper.updateEntity(dto, product);
            Product updated = productRepository.save(product);
            return productMapper.toDTO(updated);
        } catch (IOException e) {
            throw new RuntimeException("Không thể upload ảnh", e);
        }
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        deleteImage(product.getImages());
        productRepository.delete(product);
    }

    private String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Files.createDirectories(uploadDir);
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;
        Path target = uploadDir.resolve(fileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }

    private void deleteImage(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            Path file = uploadDir.resolve(fileName);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("Không thể xóa ảnh: " + fileName);
        }
    }
}

package com.tonsaito.ws.products.controller;

import com.tonsaito.ws.products.model.ProductModel;
import com.tonsaito.ws.products.model.ResponseModel;
import com.tonsaito.ws.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<ResponseModel> createProduct(@RequestBody ProductModel productModel){
        String productId = null;
        try{
            productId = productService.createProductSync(productModel);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(e.getMessage(), "/products"));
        }


        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseModel("Created product ID", productId));
    }
}

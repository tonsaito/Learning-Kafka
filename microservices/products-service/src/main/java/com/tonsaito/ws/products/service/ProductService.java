package com.tonsaito.ws.products.service;

import com.tonsaito.ws.products.model.ProductModel;

public interface ProductService {

    String createProductAsync(ProductModel productModel);
    String createProductSync(ProductModel productModel) throws Exception;
}

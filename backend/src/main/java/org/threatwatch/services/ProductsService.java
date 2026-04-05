package org.threatwatch.services;

import org.threatwatch.models.ProductModel;

import java.util.List;
import java.util.Map;

public interface ProductsService {

    public Map<String, List<ProductModel>> getProducts();
    public boolean isSupportedProduct(String product);
    public String normalizeProduct(String product);

}

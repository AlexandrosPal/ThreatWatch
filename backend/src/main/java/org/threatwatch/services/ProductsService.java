package org.threatwatch.services;

import org.threatwatch.models.ProductModel;

import java.util.ArrayList;
import java.util.Map;

public interface ProductsService {

    public Map<String, ArrayList<ProductModel>> getProducts();

}

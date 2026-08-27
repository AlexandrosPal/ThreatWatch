package org.threatwatch.products;

import java.util.Set;

public interface ProductsService {

    public Set<String> getProducts();
    public boolean isSupportedProduct(String product);
    public String normalizeProduct(String product);

}

package org.threatwatch.products;

public class ProductModel {
    private String vendor;
    private String product;
    private String displayName;
    private String part;

    public String getVendor() { return vendor; }

    public void setVendor(String vendor) { this.vendor = vendor; }

    public String getProduct() { return product; }

    public void setProduct(String product) { this.product = product; }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPart() { return part; }

    public void setPart(String part) { this.part = part; }
}

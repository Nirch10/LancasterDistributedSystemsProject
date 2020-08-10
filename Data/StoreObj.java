package server.src.Data;


import java.util.List;

public class StoreObj {
    public double price;
    public String name;
    public List<Size> sizesAvailable;
    public boolean onSale;

    public StoreObj(String product_name, double product_price, List<Size> sizes, boolean isOnSale){
        name = product_name;
        price = product_price;
        sizesAvailable = sizes;
        onSale = isOnSale;
    }
}

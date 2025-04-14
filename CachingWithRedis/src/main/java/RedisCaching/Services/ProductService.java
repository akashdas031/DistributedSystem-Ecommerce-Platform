package RedisCaching.Services;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import RedisCaching.Entities.Product;

public interface ProductService {
	Product createProduct(Product product);
	List<Product> getAllProducts();
	Product getSinleProduct(String productId);
	Product updateProduct(Product product,String productId);
	boolean removeProduct(String productId);
	List<Product> getProductsByProductName(String productName);
}

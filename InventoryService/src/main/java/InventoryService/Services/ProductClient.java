package InventoryService.Services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import InventoryService.Entities.Product;

@FeignClient(name="CachingWithRedis",url="http://localhost:3496/ProductService")
public interface ProductClient {

	@GetMapping("/getSingleProduct/{productId}")
	Product getProduct(@PathVariable("productId") String productId);
}

package RedisCaching.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import RedisCaching.Entities.Product;
import RedisCaching.Services.ProductService;

@RestController
@RequestMapping("ProductService/")
public class ProductController {
	
	private ProductService productServ;
	
	public ProductController(ProductService productServ) {
		this.productServ=productServ;
	}
	
	@PostMapping("/createProduct")
	public ResponseEntity<Product> createProduct(@RequestBody Product product){
		Product createdProd = this.productServ.createProduct(product);
		return new ResponseEntity<Product>(createdProd,HttpStatus.CREATED);
	}
	
	@GetMapping("/viewProducts")
	public ResponseEntity<List<Product>> getAllProductList(){
		List<Product> allProducts = this.productServ.getAllProducts();
		return new ResponseEntity<List<Product>>(allProducts,HttpStatus.OK);
	}
	
	@GetMapping("/getSingleProduct/{productId}")
	public ResponseEntity<Product> getSingleProduct(@PathVariable("productId") String productId){
		Product singleProduct = this.productServ.getSinleProduct(productId);
		return new ResponseEntity<Product>(singleProduct,HttpStatus.OK);
	}
	
	@PutMapping("/updateProduct/{productId}")
	public ResponseEntity<Product> updateProduct(@RequestBody Product product,@PathVariable("productId") String productId){
		Product updatedProduct = this.productServ.updateProduct(product, productId);
		return new ResponseEntity<Product>(updatedProduct,HttpStatus.OK);
	}
	@DeleteMapping("/deleteProduct/{productId}")
	public ResponseEntity<Map<String,String>> deleteProduct(@PathVariable("productId") String productId){
		Map<String,String> map=new HashMap<>();
		boolean isAvailable = this.productServ.removeProduct(productId);
		
		if(!isAvailable) {
			map.put("Status", "Success");
			map.put("Message", "Product with Id : "+productId+" Has been removed from the Server");
			return new ResponseEntity<Map<String,String>>(map,HttpStatus.OK);
		}
		else {
			map.put("Status", "Failure");
			map.put("Message", "Something went wrong");
			return new ResponseEntity<Map<String,String>>(map,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/getProductByName/{productName}")
	public ResponseEntity<List<Product>> getProductsWithName(@PathVariable("productName") String productName){
		List<Product> product = this.productServ.getProductsByProductName(productName);
		return new ResponseEntity<List<Product>>(product,HttpStatus.OK);
	}

}

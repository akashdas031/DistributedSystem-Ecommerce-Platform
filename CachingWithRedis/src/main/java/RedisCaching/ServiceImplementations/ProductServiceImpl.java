package RedisCaching.ServiceImplementations;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import RedisCaching.Entities.Product;
import RedisCaching.Repositories.ProductRepo;
import RedisCaching.Services.ProductService;
import jakarta.transaction.Transactional;

@Service
public class ProductServiceImpl implements ProductService{
	
	private Logger logger=LoggerFactory.getLogger(ProductServiceImpl.class);
	
	private ProductRepo productRepo;
	private CacheManager cacheManager;
	
	public ProductServiceImpl(ProductRepo productRepo,CacheManager cacheManager) {
		this.productRepo=productRepo;
		this.cacheManager=cacheManager;
	}

	@Override
	@CachePut(value="product",key="#result.productId")
	@Transactional
	public Product createProduct(Product product) {
		String prodId = UUID.randomUUID().toString().substring(10).replaceAll("-", "").trim();
		product.setProductId(prodId);
		return this.productRepo.save(product);
	}

	@Override
	public List<Product> getAllProducts() {
		
		return this.productRepo.findAll();
	}

	@Override
	@CachePut(value="product",key="#productId")
	public Product getSinleProduct(String productId) {
		
		logger.info("ProductDetails Fetch from the DB......");
		return this.productRepo.findById(productId).orElseThrow(()-> new RuntimeException("User With Given Id is Not available in the server"));
	}

	@Override
	@CachePut(value="product",key="#productId")
	public Product updateProduct(Product product, String productId) {
		Product existingProduct = this.productRepo.findById(productId).orElseThrow(()-> new RuntimeException("Product with Id not exist in the server...can't update"));
		existingProduct.setProductName(product.getProductName());
		existingProduct.setProductDEscription(product.getProductDEscription());
		existingProduct.setProductPrice(product.getProductPrice());
		return this.productRepo.save(existingProduct);
	}

	@Override
	@CacheEvict(value="product",key="#productId",allEntries = true)
	public boolean removeProduct(String productId) {
		this.productRepo.deleteById(productId);
		return this.productRepo.existsById(productId);
		
	}

	@Override
	public List<Product> getProductsByProductName(String productName) {
		List<Product> product= this.productRepo.findByProductName(productName);
		Cache cache = this.cacheManager.getCache("productId");
		if(cache != null) {
			cache.put(product, "productId");
		}else {
			logger.info("Product with given details was not in the cache...fetched from db and added in cache...");
			cache.put(product, "productId");
		}
		return product;
	}

}

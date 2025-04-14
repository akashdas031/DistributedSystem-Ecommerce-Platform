package RedisCaching.RedisDataStorageTypes;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import RedisCaching.Entities.Product;

@Service
public class RedisHashService {

	private RedisTemplate<String,Object> redisTemplate;
	public RedisHashService(RedisTemplate<String,Object> redisTemplate) {
		this.redisTemplate=redisTemplate;
	}
	
	private static final String key="PRODUCT_HASH";
	
	//add Product to the redis Hash
	public void addToRedisHash(Product product) {
		this.redisTemplate.opsForHash().put(key, product.getProductId(), product);
	}
	
	//get Product from the redis hash
	public Object getProductFromRedisHash(String productId) {
		return this.redisTemplate.opsForHash().get(key, productId);
	}
	
	//delete Product from the redis Hash
	public void removeFromRedisHash(String productId) {
		this.redisTemplate.opsForHash().delete(key, productId);
	}
}

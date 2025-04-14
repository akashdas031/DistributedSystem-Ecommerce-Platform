package RedisCaching.RedisDataStorageTypes;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import RedisCaching.Entities.Product;

@Service
public class RedisSetService {

	private RedisTemplate<String,Object> redisTemplate;
	public RedisSetService(RedisTemplate<String,Object> redisTemplate) {
		this.redisTemplate=redisTemplate;
	}
	
	//Redis Key
	private static final String key="PRODUCT_SET";
	
	//add to the redis set
	public void addToRedisSet(Product product) {
		this.redisTemplate.opsForSet().add(key, product);
	}
	
	//list all products from set
	
	public Set<Object> viewAllProductsFromSet(){
		return this.redisTemplate.opsForSet().members(key);
	}
	
	//remove from the set
	
	public void removeFromTheSet(Product product) {
		this.redisTemplate.opsForSet().remove(key, product);
	}
	
	//check if a product is available in the set or not
	public boolean isAvailableInSet(Product product) {
		return this.redisTemplate.opsForSet().isMember(key, product);
	}
	
	
}

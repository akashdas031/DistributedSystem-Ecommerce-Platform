package RedisCaching.RedisDataStorageTypes;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import RedisCaching.Entities.Product;

@Service
public class RedisSortedSetService {

	private RedisTemplate<String,Product> redisTemplate;
	
	public RedisSortedSetService(RedisTemplate<String,Product> redisTemplate) {
		this.redisTemplate=redisTemplate;
	}
	
	private static final String key="SORTED_PRODUCTS";
	
	//add product to the redis sorted set
	public void addProductToRedisSortedSet(Product product,double ratings) {
		this.redisTemplate.opsForZSet().add(key, product, ratings);
	}
	
	//get top N products by rating
	public Set<Product> getTopNProducts(int count){
		//count means the number of products want to view and the 0 is the start range and reverse range is to sort the set in 
		//Descending order
		return this.redisTemplate.opsForZSet().reverseRange(key, 0, count-1);
	}
	
	//remove product from sorted set of redis
	public void removeProductFromRedisSortedSet(String productId) {
		this.redisTemplate.opsForZSet().remove(key, productId);
	}
	
}

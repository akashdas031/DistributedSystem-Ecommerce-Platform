package RedisCaching.RedisDataStorageTypes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import RedisCaching.Entities.Product;

@Service
public class RedisListService {

	private RedisTemplate<String,Object> redisTemplate;
	
	public RedisListService(RedisTemplate<String,Object> redisTemplate) {
		this.redisTemplate=redisTemplate;
	}
	private Logger logger=LoggerFactory.getLogger(RedisListService.class);
	private static final String key="PRODUCT";
	//cache data to the redis list
	public void addToList(Product product) {
		this.redisTemplate.opsForList().rightPush(key, product);
		logger.info("product added to the redis list....");
	}
	
	//get the product list
	public List<Object> getProducts(){
		//here range 0 to -1 indicates the total data present in the redis list
		return this.redisTemplate.opsForList().range(key, 0, -1);
	}
	
	//remove product from the list
	public void removeProductFromList(Product product) {
		redisTemplate.opsForList().remove(key, 1, product);
	}
	
}

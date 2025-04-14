package RedisCaching.RedisDataStorageTypes;

import java.util.Map;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import RedisCaching.Entities.Product;

@Service
public class RedisStreamPublisher {

	private RedisTemplate<String,Object> redisTemplate;
	public RedisStreamPublisher(RedisTemplate<String,Object> redisTemplate) {
		this.redisTemplate=redisTemplate;
	}
	
	private static final String key="Product-Stream";
	public void createConsumerGroup(RedisConnectionFactory connectionFactory,StringRedisTemplate redisTemplate) {
		String streamName="Product-Stream";
		String consumerGroup="Product-Group";
		redisTemplate.opsForStream().createGroup(streamName, consumerGroup);
	}
	public void publishProduct(Product product) {
		this.redisTemplate.opsForStream().add(key, Map.of(
				"productId",product.getProductId(),
				"productName",product.getProductName(),
				"productDescription",product.getProductDEscription()
				));
	}
}

package RedisCaching.Configurations;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.StreamReceiver;
import org.springframework.data.redis.stream.Subscription;

import RedisCaching.Entities.Product;
import RedisCaching.RedisDataStorageTypes.RedisStreamSubscriber;

@Configuration
public class CacheConfig {

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)).disableCachingNullValues();
		return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(configuration).build();
	}
	
	//Redis Template
	@Bean
	public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
		RedisTemplate<String, Object> redisTemplate=new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		return redisTemplate;
	}
	@Bean
	public RedisTemplate<String,Product> redisTemplateN(RedisConnectionFactory redisConnectionFactory){
		RedisTemplate<String, Product> redisTemplate=new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		return redisTemplate;
	}
	
	@Bean
	public Subscription redisSubscriber(RedisConnectionFactory redisConnectionFactory,RedisStreamSubscriber subscriber,StringRedisTemplate redisTemplate) {
		//createConsumerGroup(redisConnectionFactory,redisTemplate);
		StreamMessageListenerContainerOptions<String, MapRecord<String,String,String>> options=StreamMessageListenerContainerOptions.builder()
																											.pollTimeout(Duration.ofSeconds(1)).batchSize(10).build();
		StreamMessageListenerContainer<String,MapRecord<String,String,String>> container=StreamMessageListenerContainer.create(redisConnectionFactory, options);
		Subscription subscription=container.receive(
				Consumer.from("Product-Group", "Consumer-1"),
				StreamOffset.create("Product-Stream",ReadOffset.lastConsumed()),
				subscriber
				);
		container.start();
		return subscription;
	}
	
}

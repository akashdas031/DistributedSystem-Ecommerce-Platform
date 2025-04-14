package InventoryService.Configurations;

import java.time.Duration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RabbitMQConfig {

	public static final String INVENTORY_QUEUE="invetory.check.queue";
	public static final String INVENTORY_EXCHANGE="inventory.exchange";
	public static final String INVENTORY_ROUTING_KEY="inventory.check";
	
	public static final String ROLLBACK_QUEUE="rollback.queue";
	public static final String ROLLBACK_EXCHANGE="rollback.exchange";
	public static final String ROLLBACK_ROUTING_KEY="rollback";
	
	@Bean
	public Queue rollbackQueue() {
		return new Queue(ROLLBACK_QUEUE,true);
	}
	
	@Bean
	public TopicExchange rollbackExchange() {
		return new TopicExchange(ROLLBACK_EXCHANGE);
	}
	
	@Bean
	public Binding bindingRollback(@Qualifier("rollbackQueue") Queue rollbackQueue,@Qualifier("rollbackExchange") TopicExchange rollbackExchange) {
		return BindingBuilder.bind(rollbackQueue).to(rollbackExchange).with(ROLLBACK_ROUTING_KEY);
	}
	
	@Bean
	public Queue inventoryQueue() {
		return new Queue(INVENTORY_QUEUE,true);
	}
	@Bean
	public TopicExchange inventoryExchange() {
		return new TopicExchange(INVENTORY_EXCHANGE);
	}
	@Bean
	public Binding binding(@Qualifier("inventoryQueue") Queue inventoryQueue,@Qualifier("inventoryExchange") TopicExchange inventoryExchange) {
		return BindingBuilder.bind(inventoryQueue).to(inventoryExchange).with(INVENTORY_ROUTING_KEY);
	}
	
	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
		RedisCacheConfiguration configuration=RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)).disableCachingNullValues();
		return RedisCacheManager.builder(factory).cacheDefaults(configuration).build();
	}
	
	@Bean
	public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory connection){
		RedisTemplate<String,Object> redisTemplate=new RedisTemplate();
		redisTemplate.setConnectionFactory(connection);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		return redisTemplate;
	}
	@Bean
	public Jackson2JsonMessageConverter jsonMessageConvertor() {
		return new Jackson2JsonMessageConverter();
	}
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,Jackson2JsonMessageConverter convertor) {
		RabbitTemplate rabbitTemplate=new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(convertor);
		return rabbitTemplate;
	}
}

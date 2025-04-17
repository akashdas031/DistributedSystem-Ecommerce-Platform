package InventoryService.Configurations;

import java.time.Duration;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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

	//Order queue consumer
	public static final String ORDER_QUEUE="order.queue";
	public static final String ORDER_EXCHANGE="order.exchange";
	public static final String ORDER_ROUTING_KEY="order";
	//Payment success Queue Consumer
	public static final String PAYMENT_SUCCESS_QUEUE="payment.success.queue";
	public static final String PAYMENT_SUCCESS_EXCHANGE="payment.success.exchange";
	public static final String PAYMENT_SUCCESS_ROUTING_KEY="payment.success";
	
	//Payment failure Queue Consumer
    public static final String PAYMENT_FAILURE_QUEUE="payment.failure.queue";
	public static final String PAYMENT_FAILURE_EXCHANGE="payment.failure.exchange";
	public static final String PAYMENT_FAILURE_ROUTING_KEY="payment.failure";
	//Inventory Success producer
	public static final String INVENTORY_SUCCESS_EXCHANGE="inventory.success.exchange";
	public static final String INVENTORY_SUCCESS_ROUTING_KEY="inventory.success";
	
	@Bean
	public TopicExchange inventorySuccessExchange() {
		return new TopicExchange(INVENTORY_SUCCESS_EXCHANGE);
	}
	
	//inventory failure producer
	public static final String INVENTORY_FAILURE_EXCHANGE="inventory.failure.exchange";
	public static final String INVENTORY_FAILURE_ROUTING_KEY="inventory.failure";
	
	@Bean
	public TopicExchange inventoryFailureExchange() {
		return new TopicExchange(INVENTORY_SUCCESS_EXCHANGE);
	}
	
	//payment producer
	public static final String PAYMENT_EXCHANGE="payment.exchange";
	public static final String PAYMENT_ROUTING_KEY="payment";
	
	@Bean
	public TopicExchange paymentExchange() {
		return new TopicExchange(PAYMENT_EXCHANGE);
	}
	
	@Bean
	public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory=new SimpleRabbitListenerContainerFactory();
		Jackson2JsonMessageConverter convertor=new Jackson2JsonMessageConverter();
		factory.setConnectionFactory(connectionFactory);
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setMessageConverter(convertor);
		return factory;
	}
	
	//Queue,Exchange and binding for payment failure Queue Consumer
	@Bean
	public Queue paymentFailureQueue() {
		return new Queue(PAYMENT_FAILURE_QUEUE,true);
	}
	
	@Bean
	public TopicExchange paymentFailureExchange() {
		return new TopicExchange(PAYMENT_FAILURE_EXCHANGE);
	}
	
	@Bean
	public Binding bindingPaymentFailure(@Qualifier("paymentFailureQueue") Queue paymentFailureQueue,@Qualifier("paymentFailureExchange") TopicExchange paymentFailureExchange) {
		return BindingBuilder.bind(paymentFailureQueue).to(paymentFailureExchange).with(PAYMENT_FAILURE_ROUTING_KEY);
	}
	
	//Queue,Exchange and binding for payment Success Queue Consumer
		@Bean
		public Queue paymentSuccessQueue() {
			return new Queue(PAYMENT_SUCCESS_QUEUE,true);
		}
		
		@Bean
		public TopicExchange paymentSuccessExchange() {
			return new TopicExchange(PAYMENT_SUCCESS_EXCHANGE);
		}
		
		@Bean
		public Binding bindingRollback(@Qualifier("paymentSuccessQueue") Queue paymentSuccessQueue,@Qualifier("paymentSuccessExchange") TopicExchange paymentSuccessExchange) {
			return BindingBuilder.bind(paymentSuccessQueue).to(paymentSuccessExchange).with(PAYMENT_SUCCESS_ROUTING_KEY);
		}
	
	//Queue,Exchange,and Binding for Order Queue Consumer
	@Bean
	public Queue orderQueue() {
		return new Queue(ORDER_QUEUE,true);
	}
	@Bean
	public TopicExchange orderExchange() {
		return new TopicExchange(ORDER_EXCHANGE);
	}
	@Bean
	public Binding binding(@Qualifier("orderQueue") Queue orderQueue,@Qualifier("orderExchange") TopicExchange orderExchange) {
		return BindingBuilder.bind(orderQueue).to(orderExchange).with(ORDER_ROUTING_KEY);
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

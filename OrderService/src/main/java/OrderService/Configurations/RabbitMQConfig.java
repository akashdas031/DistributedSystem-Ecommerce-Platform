package OrderService.Configurations;

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
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

	public static final String ORDER_EXCHANGE="order.exchange";
	public static final String ORDER_ROUTING_KEY="order";
	
//	public static final String PAYMENT_EXCHANGE="payment.exchange";
//	public static final String PAYMENT_ROUTING_KEY="payment";
	
	public static final String INVENTORY_SUCCESS_QUEUE="inventory.success.queue";
	public static final String INVENTORY_SUCCESS_EXCHANGE="inventory.success.exchange";
	public static final String INVENTORY_SUCCESS_ROUTING_KEY="inventory.success";
	
	public static final String INVENTORY_FAILURE_QUEUE="inventory.failure.queue";
	public static final String INVENTORY_FAILURE_EXCHANGE="inventory.failure.exchange";
	public static final String INVENTORY_FAILURE_ROUTING_KEY="inventory.failure";
	
	@Bean
	public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		Jackson2JsonMessageConverter convertor=new Jackson2JsonMessageConverter();
		SimpleRabbitListenerContainerFactory factory=new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setMessageConverter(convertor);
		return factory;
	}
	@Bean
	public TopicExchange orderExchange() {
		return new TopicExchange(ORDER_EXCHANGE);
	}
	
	@Bean
	public Queue inventorySuccessQueue() {
		return new Queue(INVENTORY_SUCCESS_QUEUE);
	}
	
	@Bean
	public TopicExchange inventorySuccessExchange() {
		return new TopicExchange(INVENTORY_SUCCESS_EXCHANGE);
	}
	
	@Bean
	public Binding bindingRollback(@Qualifier("inventorySuccessQueue") Queue inventorySuccessQueue,@Qualifier("inventorySuccessExchange") TopicExchange inventorySuccessExchange) {
		return BindingBuilder.bind(inventorySuccessQueue).to(inventorySuccessExchange).with(INVENTORY_SUCCESS_ROUTING_KEY);
	}
	
	@Bean
	@Primary
	public Queue inventoryFailureQueue() {
		return new Queue(INVENTORY_FAILURE_QUEUE);
	}
	
	@Bean
	public TopicExchange inventoryFailureExchange() {
		return new TopicExchange(INVENTORY_FAILURE_EXCHANGE);
	}
	
	@Bean
	public Binding binding(@Qualifier("inventoryFailureQueue") Queue inventoryFailureQueue,@Qualifier("inventoryFailureExchange") TopicExchange inventoryFailureExchange) {
		return BindingBuilder.bind(inventoryFailureQueue).to(inventoryFailureExchange).with(INVENTORY_FAILURE_ROUTING_KEY);
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

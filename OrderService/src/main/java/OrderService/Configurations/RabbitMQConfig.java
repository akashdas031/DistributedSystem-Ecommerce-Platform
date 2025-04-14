package OrderService.Configurations;

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
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

	public static final String INVENTORY_EXCHANGE="inventory.exchange";
	public static final String INVENTORY_ROUTING_KEY="inventory.check";
	
	public static final String PAYMENT_EXCHANGE="payment.exchange";
	public static final String PAYMENT_ROUTING_KEY="payment";
	
	public static final String ROLLBACK_QUEUE="rollback.queue";
	public static final String ROLLBACK_EXCHANGE="rollback.exchange";
	public static final String ROLLBACK_ROUTING_KEY="rollback";
	
	public static final String PAYMENT_STATUS_QUEUE="paymentStatus.queue";
	public static final String PAYMENT_STATUS_EXCHANGE="paymentStatus.exchange";
	public static final String PAYMENT_STATUS_ROUTING_KEY="paymentStatus";
	
	@Bean
	public Queue paymentStatusQueue() {
		return new Queue(PAYMENT_STATUS_QUEUE);
	}
	
	@Bean
	public TopicExchange paymentStatusExchange() {
		return new TopicExchange(PAYMENT_STATUS_EXCHANGE);
	}
	
	@Bean
	public Binding bindingPaymentStatus(@Qualifier("paymentStatusQueue") Queue paymentStatusQueue,@Qualifier("paymentStatusExchange") TopicExchange paymentStatusExchange) {
		return BindingBuilder.bind(paymentStatusQueue).to(paymentStatusExchange).with(PAYMENT_STATUS_ROUTING_KEY);
	}
	
	@Bean
	@Primary
	public Queue rollbackQueue() {
		return new Queue(ROLLBACK_QUEUE);
	}
	
	@Bean
	public TopicExchange rollbackExchange() {
		return new TopicExchange(ROLLBACK_EXCHANGE);
	}
	
	
	@Bean
	public TopicExchange paymentExchange() {
		return new TopicExchange(PAYMENT_EXCHANGE);
	}
	@Bean
	public Binding binding(@Qualifier("rollbackQueue") Queue rollbackQueue,@Qualifier("rollbackExchange") TopicExchange rollbackExchange) {
		return BindingBuilder.bind(rollbackQueue).to(rollbackExchange).with(ROLLBACK_ROUTING_KEY);
	}
	
	@Bean
	public TopicExchange exchange() {
		return new TopicExchange(INVENTORY_EXCHANGE);
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

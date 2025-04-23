package OrderService.ServiceImplementations;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import OrderService.Configurations.RabbitMQConfig;
import OrderService.Entities.Order;
import OrderService.Entities.RollBackEvent;
import OrderService.Enums.OrderStatus;
import OrderService.Enums.PaymentStatus;
import OrderService.Repositories.OrderRepository;
import OrderService.Services.OrderService;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

@Service
public class OrderServiceImpl implements OrderService{

	private Logger logger=LoggerFactory.getLogger(OrderServiceImpl.class);
	private OrderRepository orderRepo;
	private RabbitTemplate rabbitTemplate;
	private Tracer tracer;
	
	public OrderServiceImpl(OrderRepository orderRepo,RabbitTemplate rabbitTemplate,Tracer tracer) {
		this.orderRepo=orderRepo;
		this.rabbitTemplate=rabbitTemplate;
		this.tracer=tracer;
	}
	
	@Override
	public Order createOrder(Order order) {
		Span span = tracer.nextSpan().name("order.queue").start();
		try {
			String orderId = UUID.randomUUID().toString().substring(10).replaceAll("-", "").trim();
			order.setOrderId(orderId);
			order.setOrderStatus(OrderStatus.CREATED);
			order.setPaymentStatus(PaymentStatus.PENDING);
			order.setOrderPlacedTime(LocalDateTime.now());
			order.setOrderUpdatedTime(LocalDateTime.now());
			this.rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE,RabbitMQConfig.ORDER_ROUTING_KEY,order);
			logger.info("Order : "+order);
			
			return this.orderRepo.save(order);
		}catch(Exception e) {
			order.setOrderStatus(OrderStatus.INTERRUPTED);
			RollBackEvent ord = RollBackEvent.builder().order(order).serviceName("ORDER_SERVICE").reason(e.getMessage()).time(LocalDateTime.now()).build();
			logger.info("Error occured in the Order Service : "+e.getMessage());
		}finally {
			span.end();
		}
		return null; 
	}
	@RabbitListener(queues=RabbitMQConfig.INVENTORY_SUCCESS_QUEUE,containerFactory = "simpleRabbitListenerContainerFactory")
	public void validatePaymentStatus(RollBackEvent status,Channel channel,Message message) throws InterruptedException, IOException {
		//Thread.currentThread().sleep(10000);
		try {
			logger.info("Status Recieved from the Inventory : "+status.getOrder().getPaymentStatus());
			Order order=status.getOrder();
			Order existingOrder = this.orderRepo.findByProductIdAndOrderId(order.getProductId(), order.getOrderId());
			existingOrder.setPaymentStatus(order.getPaymentStatus());
			existingOrder.setOrderStatus(OrderStatus.PLACED);
			logger.info("Order After Update : "+existingOrder);
			this.orderRepo.save(existingOrder);
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
		}
		catch(Exception e) {
			logger.info(e.getMessage());
			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
		}
		
	}
	
	@Override
	public List<Order> getAllOrderList() {
		
		return this.orderRepo.findAll();
	}

	@Override
	public Order getSingleOrderDetails(String orderId) {
		Order order = this.orderRepo.findById(orderId).orElseThrow(()->new RuntimeException("Order with given details is not available in the server..."));
		return order;
	}

	@Override
	public List<Order> getOrdersForProduct(String productId) {
		List<Order> products = this.orderRepo.findByProductId(productId);
		return products;
	}

	@Override
	public Order updateOrder(Order order, String productId,String orderId) {
		Order stock = this.orderRepo.findByProductIdAndOrderId(productId,orderId);
		stock.setOrderId(orderId);
		stock.setProductId(productId);
		stock.setOrderUpdatedTime(LocalDateTime.now());
		stock.setOrderStatus(OrderStatus.UPDATED);
		stock.setPaymentStatus(PaymentStatus.PENDING);
		stock.setQuantity(order.getQuantity());
		stock.setPrice(order.getPrice());
		Order updatedOrder = this.orderRepo.save(stock);
		return updatedOrder;
	}
	@RabbitListener(queues=RabbitMQConfig.INVENTORY_FAILURE_QUEUE,containerFactory = "simpleRabbitListenerContainerFactory")
	public void rollbackForOrderService(RollBackEvent event,Channel channel,Message message) throws InterruptedException, IOException {
		try {
			Order order = event.getOrder();
			logger.info("Reason for Rollback : "+event.getOrder().getOrderStatus());
			
			Order updatedOrder = this.updateOrder(order, order.getProductId(), order.getOrderId());
			updatedOrder.setOrderStatus(OrderStatus.CANCELLED);
			updatedOrder.setPaymentStatus(PaymentStatus.CANCELLED);
			this.orderRepo.save(updatedOrder);
			logger.info("Order has been rolled back...:"+updatedOrder);
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
		}catch(Exception e) {
			logger.info(e.getMessage());
			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
		}
	}
}

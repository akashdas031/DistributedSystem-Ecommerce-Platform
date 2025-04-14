package OrderService.ServiceImplementations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import OrderService.Configurations.RabbitMQConfig;
import OrderService.Entities.Order;
import OrderService.Entities.RollBackEvent;
import OrderService.Enums.OrderStatus;
import OrderService.Enums.PaymentStatus;
import OrderService.Repositories.OrderRepository;
import OrderService.Services.OrderService;

@Service
public class OrderServiceImpl implements OrderService{

	private Logger logger=LoggerFactory.getLogger(OrderServiceImpl.class);
	private OrderRepository orderRepo;
	private RabbitTemplate rabbitTemplate;
	
	public OrderServiceImpl(OrderRepository orderRepo,RabbitTemplate rabbitTemplate) {
		this.orderRepo=orderRepo;
		this.rabbitTemplate=rabbitTemplate;
	}
	
	@Override
	public Order createOrder(Order order) {
		try {
			String orderId = UUID.randomUUID().toString().substring(10).replaceAll("-", "").trim();
			order.setOrderId(orderId);
			order.setOrderStatus(OrderStatus.CREATED);
			order.setPaymentStatus(PaymentStatus.PENDING);
			order.setOrderPlacedTime(LocalDateTime.now());
			order.setOrderUpdatedTime(LocalDateTime.now());
			this.rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_EXCHANGE,RabbitMQConfig.INVENTORY_ROUTING_KEY,order);
			logger.info("Order : "+order);
			this.rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE,RabbitMQConfig.PAYMENT_ROUTING_KEY,order);
//			if(true) {
//				throw new RuntimeException("Some Error Occured Here...");
//			}
			
			return this.orderRepo.save(order);
		}catch(Exception e) {
			order.setOrderStatus(OrderStatus.INTERRUPTED);
			RollBackEvent ord = RollBackEvent.builder().order(order).serviceName("ORDER_SERVICE").reason(e.getMessage()).time(LocalDateTime.now()).build();
			this.rabbitTemplate.convertAndSend(RabbitMQConfig.ROLLBACK_EXCHANGE,RabbitMQConfig.ROLLBACK_ROUTING_KEY,ord);
			logger.info("Error occured in the Order Service : "+e.getMessage());
		}
		return null; 
	}
	@RabbitListener(queues=RabbitMQConfig.PAYMENT_STATUS_QUEUE)
	public void validatePaymentStatus(String status) throws InterruptedException {
		//Thread.currentThread().sleep(10000);
		logger.info("Status Recieved from the PaymentService : "+status);
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
	@RabbitListener(queues=RabbitMQConfig.ROLLBACK_QUEUE)
	public void rollbackForOrderService(RollBackEvent event) throws InterruptedException {
		Thread.currentThread().sleep(1000);
		Order order = event.getOrder();
		logger.info("Reason for Rollback : "+event.getOrder().getOrderStatus());
		
		Order updatedOrder = this.updateOrder(order, order.getProductId(), order.getOrderId());
		logger.info("Order has been rolled back...:"+updatedOrder);
	}
}

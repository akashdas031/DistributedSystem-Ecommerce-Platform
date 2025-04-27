package InventoryService.ServiceImpls;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import InventoryService.Configurations.RabbitMQConfig;
import InventoryService.Entities.Inventory;
import InventoryService.Entities.Order;
import InventoryService.Entities.PaymentRequestDTO;
import InventoryService.Entities.Product;
import InventoryService.Entities.RollBackEvent;
import InventoryService.Enums.OrderStatus;
import InventoryService.Enums.PaymentStatus;
import InventoryService.Repositories.InventoryRepository;
import InventoryService.Services.InventoryService;
import InventoryService.Services.ProductClient;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

@Service
public class InventoryServiceImpl implements InventoryService{

	private Logger logger=LoggerFactory.getLogger(InventoryServiceImpl.class);
	
	private InventoryRepository inventoryRepo;
	private ProductClient productClient;
	private RedisTemplate<String,Object> redisTemplate;
	private RedisTemplate<String,Order> redisTemplateO;
	private RabbitTemplate rabbitTemplate;
	private Tracer tracer;
	public InventoryServiceImpl(InventoryRepository inventoryRepo,ProductClient productClient,RedisTemplate<String,Object> redisTemplate,RabbitTemplate rabbitTemplate,
			                    RedisTemplate<String,Order> redisTemplateO,Tracer tracer) {
		this.inventoryRepo=inventoryRepo;
		this.productClient=productClient;
		this.redisTemplate=redisTemplate;
		this.rabbitTemplate=rabbitTemplate;
		this.redisTemplateO=redisTemplateO;
		this.tracer=tracer;
	}
	
	@Override
	public Inventory addProductToInventory(Inventory inventory) {
		String inventoryId = UUID.randomUUID().toString().substring(10).replaceAll("-", "").trim();
		inventory.setInventoryId(inventoryId);
		return this.inventoryRepo.save(inventory);
	}

	@Override
	public List<Inventory> getAllProductsFromInventory() {
		
		return this.inventoryRepo.findAll();
	}

	@Override
	public Inventory getSingleProductFromInventory(String productId) {
		
		return this.inventoryRepo.findByProductId(productId);
	}

	@Override
	public Inventory getInventory(String inventoryId) {
		
		return this.inventoryRepo.findById(inventoryId).orElseThrow(()->(new RuntimeException("Wrong inventory Information...Can't Fetch from server")));
	}

	@Override
	public Inventory updateInventory(Inventory inventory, String productId) {
		Inventory product = this.inventoryRepo.findByProductId(productId);
		product.setProductId(productId);
		product.setQuantityAvailable(inventory.getQuantityAvailable());
		Inventory updatedProduct = this.inventoryRepo.save(product);
		return updatedProduct;
	}
	
	@RabbitListener(queues=RabbitMQConfig.ORDER_QUEUE,containerFactory = "simpleRabbitListenerContainerFactory")
	public void handleInventoryCheck(Order order,Channel channel,Message message) throws InterruptedException, IOException {
		Span span = tracer.nextSpan().name("order.queue.consumer").start();
		try(var ignored=tracer.withSpan(span)) {
			span.tag("message","Order with id : "+ order.getOrderId()+" recieved from Order Service . Order Details : "+order);
			String productId=order.getProductId();
			int requestedQuantity=Integer.valueOf(order.getQuantity());
			//get from the product client that the product is available or not ..it's optional but i generate a synchronous communication here
			Product product = this.productClient.getProduct(productId);
			if(product==null) {
				throw new RuntimeException("Product Details Not available in the server...");
			}
			//check whether the product is available in the inventory or not if available then check for quantity is sufficient for the requested quantity or not 
			//if not sufficient throw exception
			//else store the current inventory in the cache
			// and update the inventory by subtracting the requested quantity 
			Inventory inventoryProduct = this.inventoryRepo.findByProductId(productId);
			int available=Integer.valueOf(inventoryProduct.getQuantityAvailable());
			if(available<requestedQuantity) {
				throw new RuntimeException("Insufficient Stock Quantity...");
			}
			double pricePaidByUser=Double.valueOf(order.getPrice());
			logger.info("Amount Paid by the User : "+pricePaidByUser);
			logger.info("Actual Product Price : "+product.getProductPrice());
			if(product.getProductPrice() != pricePaidByUser) {
				logger.info("Price not Equal ...something wrong in the throwing exception");
				throw new RuntimeException("Invalid Amount...Pay valid Amount...");
			}
			this.redisTemplate.opsForValue().set("Order : "+productId, order.getOrderId());
			this.redisTemplate.opsForValue().set("inventory :"+order.getProductId(), inventoryProduct.getQuantityAvailable());
			inventoryProduct.setQuantityAvailable(String.valueOf(available-requestedQuantity));
			PaymentRequestDTO paymentDetails = PaymentRequestDTO.builder().productId(productId).amount(pricePaidByUser).build();
			this.rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE,RabbitMQConfig.PAYMENT_ROUTING_KEY,paymentDetails);
            
			this.inventoryRepo.save(inventoryProduct);
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
		}catch(RuntimeException ex) {
			logger.info(ex.getMessage());
			order.setOrderStatus(OrderStatus.INTERRUPTED);
			RollBackEvent rollback = RollBackEvent.builder().order(order).serviceName("INVENTORY_SERVICE").reason(ex.getMessage()).time(LocalDateTime.now()).build();
			//Thread.currentThread().sleep(10000);
			span.tag("error_message", "Something went wrong , Error Message : "+ex.getMessage());
			this.rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_FAILURE_EXCHANGE,RabbitMQConfig.INVENTORY_FAILURE_ROUTING_KEY,rollback);
		    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
		}
		catch(Exception e) {
			logger.info(e.getMessage());
			order.setOrderStatus(OrderStatus.INTERRUPTED);
			RollBackEvent rollback = RollBackEvent.builder().order(order).serviceName("INVENTORY_SERVICE").reason(e.getMessage()).time(LocalDateTime.now()).build();
			//Thread.currentThread().sleep(10000);
			span.tag("error_message", "Something went wrong , Error Message : "+e.getMessage());
			this.rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_FAILURE_EXCHANGE,RabbitMQConfig.INVENTORY_FAILURE_ROUTING_KEY,rollback);
		
		}finally {
			span.end();
		}
	}
	@RabbitListener(queues=RabbitMQConfig.PAYMENT_FAILURE_QUEUE,containerFactory = "simpleRabbitListenerContainerFactory")
	public void RollBackForInventory(RollBackEvent rollback,Message message,Channel channel) throws IOException {
		Span span=tracer.nextSpan().name("payment.queue.failure.cunsumer").start();
		try {
			span.tag("message", "Status Recieved from Payment Queue after Payment :"+rollback.getReason());
			logger.info("Status From Payment Service : "+rollback.getReason());
			String productId=rollback.getOrder().getProductId();
			Object cacheQty=redisTemplate.opsForValue().get("inventory :"+productId);
			if(cacheQty != null) {
				Inventory inventoryProduct = this.inventoryRepo.findByProductId(productId);
				inventoryProduct.setQuantityAvailable(cacheQty.toString());
				this.inventoryRepo.save(inventoryProduct);
				this.redisTemplate.delete("inventory :"+productId);
				this.rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_FAILURE_EXCHANGE,RabbitMQConfig.INVENTORY_FAILURE_ROUTING_KEY,rollback);
			}
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
		}catch(Exception e) {
			span.tag("error_message", "Something went wrong with payment ,Error Message : "+e.getMessage());
			logger.info(e.getMessage());
			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
		}finally {
			span.end();
		}
	}
	@RabbitListener(queues=RabbitMQConfig.PAYMENT_SUCCESS_QUEUE,containerFactory = "simpleRabbitListenerContainerFactory")
	public void paymentSuccess(RollBackEvent rollback,Message message,Channel channel) throws IOException {
		Span span=tracer.nextSpan().name("payment.queue.success.consumer").start();
		try {
			span.tag("message", "Status Recieved from Payment Service : "+rollback.getReason());
			Object order = this.redisTemplate.opsForValue().get("Order : "+rollback.getOrder().getProductId());
			logger.info("Order from Redis : "+order.toString());
			Order build = Order.builder().orderId(order.toString()).productId(rollback.getOrder().getProductId()).paymentStatus(PaymentStatus.SUCCESSFUL).orderUpdatedTime(LocalDateTime.now()).build();
			//this.redisTemplateO.opsForValue().set("OrderID : "+order.toString(), build);
			logger.info("Status From Payment Service : "+rollback.getReason());
			rollback.setOrder(build);
			rollback.setServiceName("INVENTORY_SERVICE");
			rollback.setReason("INVENTORY_SUCCESS");
//			String productId=rollback.getOrder().getProductId();
//			Object cacheQty=redisTemplate.opsForValue().get("inventory :"+productId);
//			if(cacheQty != null) {
//				Inventory inventoryProduct = this.inventoryRepo.findByProductId(productId);
//				//inventoryProduct.setQuantityAvailable(cacheQty.toString());
				//this.inventoryRepo.save(inventoryProduct);
				//this.redisTemplate.delete("inventory :"+productId);
				this.rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_SUCCESS_EXCHANGE,RabbitMQConfig.INVENTORY_SUCCESS_ROUTING_KEY,rollback);
			//}
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
		}catch(Exception e) {
			span.tag("error_message", "Status From Payment Service : "+e.getMessage());
			logger.info(e.getMessage());
			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
		}finally {
			span.end();
		}
	}

}

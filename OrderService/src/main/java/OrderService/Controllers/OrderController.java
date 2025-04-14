package OrderService.Controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import OrderService.Entities.Order;
import OrderService.Services.OrderService;

@RestController
@RequestMapping("/OrderService")
public class OrderController {

	private Logger logger=LoggerFactory.getLogger(OrderController.class);
	private OrderService orderService;
	public OrderController(OrderService orderService) {
		this.orderService=orderService;
	}
	
	@PostMapping("/createOrder")
	public ResponseEntity<Order> createOrder(@RequestBody Order order){
		Order createdOrder = this.orderService.createOrder(order);
		return new ResponseEntity<Order>(createdOrder,HttpStatus.CREATED);
	}
	
	@GetMapping("/getAllOrders")
	public ResponseEntity<List<Order>> getAllOrders(){
		List<Order> allOrderList = this.orderService.getAllOrderList();
		return new ResponseEntity<List<Order>>(allOrderList,HttpStatus.OK);
	}
	
	@GetMapping("/getSingleOrder/{orderId}")
	public ResponseEntity<Order> getSingleOrder(@PathVariable("orderId") String orderId){
		Order order = this.orderService.getSingleOrderDetails(orderId);
		return new ResponseEntity<Order>(order,HttpStatus.OK);
	}
	
	@GetMapping("/getOrderDetailsOfProduct/{productId}")
	public ResponseEntity<List<Order>> getOrderDetailsByProduct(@PathVariable("productId") String productId){
		List<Order> ordersForProduct = this.orderService.getOrdersForProduct(productId);
		return new ResponseEntity<List<Order>>(ordersForProduct,HttpStatus.OK);
	}
	
	@PatchMapping("/updateOrder/{productId}/{orderId}")
	public ResponseEntity<Order> updateProduct(@RequestBody Order order,@PathVariable("productId") String productId,@PathVariable("orderId")String orderId){
		Order updatedOrder = this.orderService.updateOrder(order, productId, orderId);
		return new ResponseEntity<Order>(updatedOrder,HttpStatus.OK);
	}
	
}

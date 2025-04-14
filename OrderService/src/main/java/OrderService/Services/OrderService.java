package OrderService.Services;

import java.util.List;

import OrderService.Entities.Order;

public interface OrderService {

	public Order createOrder(Order order);
	public List<Order> getAllOrderList();
	public Order getSingleOrderDetails(String orderId);
	public List<Order> getOrdersForProduct(String productId);
	public Order updateOrder(Order order,String productId,String orderId);
}

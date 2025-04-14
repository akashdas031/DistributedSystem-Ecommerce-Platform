package OrderService.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import OrderService.Entities.Order;

public interface OrderRepository extends JpaRepository<Order, String>{
	@Query("select o from Order o where o.productId= :productId")
	List<Order> findByProductId( @Param("productId") String productId);
	@Query("select o from Order o where o.productId= :productId AND o.orderId= :orderId")
	Order findByProductIdAndOrderId(@Param("productId") String productId,@Param("orderId") String orderId);
}

package InventoryService.Entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import InventoryService.Enums.OrderStatus;
import InventoryService.Enums.PaymentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order implements Serializable{
	
	
	private String orderId;
	private String productId;
	private String quantity;
	private String price;
	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;
	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;
	private LocalDateTime orderPlacedTime;
	private LocalDateTime orderUpdatedTime;
}

package InventoryService.Entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import InventoryService.Enums.OrderStatus;
import InventoryService.Enums.PaymentStatus;
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
public class PaymentRequestDTO implements Serializable{

	private String productId;
    private double amount;
}

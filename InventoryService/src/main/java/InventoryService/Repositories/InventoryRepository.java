package InventoryService.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import InventoryService.Entities.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, String>{
	@Query("SELECT i FROM Inventory i where i.productId= :productId")
	Inventory findByProductId(@Param("productId") String productId);
}

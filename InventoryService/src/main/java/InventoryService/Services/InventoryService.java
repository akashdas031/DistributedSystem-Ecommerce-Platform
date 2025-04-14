package InventoryService.Services;

import java.util.List;

import InventoryService.Entities.Inventory;

public interface InventoryService {

	Inventory addProductToInventory(Inventory inventory);
	List<Inventory> getAllProductsFromInventory();
	Inventory getSingleProductFromInventory(String productId);
	Inventory getInventory(String inventoryId);
	Inventory updateInventory(Inventory inventory,String productId);
}

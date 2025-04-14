package InventoryService.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import InventoryService.Entities.Inventory;
import InventoryService.Services.InventoryService;

@RestController
@RequestMapping("/Inventory")
public class InventoryController {

	private Logger logger=LoggerFactory.getLogger(InventoryController.class);
	
	private InventoryService inventoryService;
	public InventoryController(InventoryService inventoryService) {
		this.inventoryService=inventoryService;
	}
	
	//add product to the inventory
	@PostMapping("/addToInventory")
	public ResponseEntity<Inventory> addProductToInventory(@RequestBody Inventory inventory){
		Inventory product = this.inventoryService.addProductToInventory(inventory);
		return new ResponseEntity<Inventory>(product,HttpStatus.CREATED);
	}
	//update Inventory
	@PostMapping("/updateInventory/{productId}")
	public ResponseEntity<Inventory> updateInventory(@RequestBody Inventory inventory,@PathVariable("productId") String productId){
		Inventory updatedProduct = this.inventoryService.updateInventory(inventory, productId);
		return new ResponseEntity<Inventory>(updatedProduct,HttpStatus.OK);
	}
	
	@GetMapping("getProductDetails/{productId}")
	public ResponseEntity<Inventory> getProductDetailsFromInventory(@PathVariable("productId") String productId){
		Inventory productDetails = this.inventoryService.getSingleProductFromInventory(productId);
		return new ResponseEntity<Inventory>(productDetails,HttpStatus.OK);
	}
}

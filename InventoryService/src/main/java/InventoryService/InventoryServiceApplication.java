package InventoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import InventoryService.Entities.NthLargest;

@SpringBootApplication
@EnableFeignClients
public class InventoryServiceApplication implements CommandLineRunner{

	private Logger logger=LoggerFactory.getLogger(InventoryServiceApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		int[] nums= {10,20,15,5,30};
		int res=NthLargest.getLargest(nums, 2);
		logger.info("REsult" + res);
	}

}

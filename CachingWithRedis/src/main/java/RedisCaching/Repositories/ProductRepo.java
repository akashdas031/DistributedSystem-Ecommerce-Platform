package RedisCaching.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import RedisCaching.Entities.Product;

public interface ProductRepo extends JpaRepository<Product,String>{
	@Query("SELECT p FROM Product p WHERE p.productName= :productName")
	List<Product> findByProductName(@Param("productName")String productName);
}

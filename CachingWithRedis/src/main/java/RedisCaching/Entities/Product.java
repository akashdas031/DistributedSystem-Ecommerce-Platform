package RedisCaching.Entities;

import java.io.Serializable;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(indexes= {@Index(name="product_index",columnList="productName")})
public class Product implements Serializable{
	
	@Id
	private String productId;
	private String productName;
	private String productDEscription;
	@ColumnDefault(value="0.0")
	private double productPrice;
}

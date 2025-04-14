package RedisCaching.RedisDataStorageTypes;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisGeospatialService {

	private Logger logger=LoggerFactory.getLogger(RedisGeospatialService.class);
	
	private RedisTemplate<String,Object> redisTemplate;
	private static final String key="REDIS_GEOSPATIAL";
	public RedisGeospatialService(RedisTemplate<String,Object> redisTemplate) {
		this.redisTemplate=redisTemplate;
	}
	 
	public void addLocation(String name,double latitude,double longitude) {
		this.redisTemplate.opsForGeo().add(key, new Point(longitude, latitude),name);
	}
	public List<Object> getNearByLocation(double latitude,double longitude,double radiusInKm){
		Circle within=new Circle(new Point(longitude,latitude),new Distance(radiusInKm,Metrics.KILOMETERS));
		GeoResults<GeoLocation<Object>> radius = this.redisTemplate.opsForGeo().radius(key, within);
		return radius.getContent().stream().map(result-> result.getContent().getName()).collect(Collectors.toList());
	}
}

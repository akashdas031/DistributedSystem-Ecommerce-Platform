package RedisCaching.Configurations;

import java.util.List;

import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroups;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class RedisStreamInitializr {

	private static final String streamName="Product-Stream";
	private static final String groupName="Product-Group";
	
	private StringRedisTemplate redisTemplate;
	
	public RedisStreamInitializr(StringRedisTemplate redisTemplate) {
		this.redisTemplate=redisTemplate;
	}
	
	@PostConstruct
	public void init() {
		StreamOperations<String,Object,Object> operations=redisTemplate.opsForStream();
		try {
			operations.info(streamName);
	}catch(RedisSystemException e){
		operations.createGroup(streamName, groupName);
	}
  }
}
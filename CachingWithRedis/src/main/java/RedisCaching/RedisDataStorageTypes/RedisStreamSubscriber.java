package RedisCaching.RedisDataStorageTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

@Service
public class RedisStreamSubscriber implements StreamListener<String, MapRecord<String,String,String>>{
	
	private Logger logger=LoggerFactory.getLogger(RedisStreamSubscriber.class);
	
	private RedisTemplate<String,Object> redisTemplate;
	
	public RedisStreamSubscriber(RedisTemplate<String,Object> redisTemplate) {
		this.redisTemplate=redisTemplate;
	}
	
	
	
	@Override
	public void onMessage(MapRecord<String, String, String> message) {
		logger.info("Message From Publisher : "+message.getValue());
		String stream=message.getStream();
		RecordId recordId=message.getId();
		redisTemplate.opsForStream().acknowledge( "Product-Group",stream, recordId);
		logger.info("Message Acknowledged...",recordId);
	}

}

package CachingTest.ServiceImpls;



import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import CachingTest.Entities.CacheUser;
import CachingTest.Repositories.CacheUserRepo;
import CachingTest.Services.CacheUserService;

@Service
@EnableCaching
public class CacheUserServiceImpl implements CacheUserService{

	private Logger logger=LoggerFactory.getLogger(CacheUserServiceImpl.class);
	private CacheUserRepo cacheRepo;
	public CacheUserServiceImpl(CacheUserRepo cacheRepo){
		this.cacheRepo=cacheRepo;
	}
	@Override
	public CacheUser createUser(CacheUser cacheUser) {
		return this.cacheRepo.save(cacheUser);
	}

	@Override
	
	public List<CacheUser> getAllUsers() {
		logger.info("Fetching all user from db");
		return this.cacheRepo.findAll();
	}
	
	@Override
	@CachePut(value = "user", key = "#userId")
	public CacheUser getSingleUser(String userId) {
		if(userId==null) {
			logger.info("User id is null in the service impl");
		}
		CacheUser user = this.cacheRepo.findById(userId).orElseThrow(()->new RuntimeException("User with given id not exist"));
		logger.info("UserDetails : "+user);
		return user;
	}

	@Override
	public CacheUser updateUser(CacheUser cacheUser, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteUser(String userId) {
		// TODO Auto-generated method stub
		
	}

}

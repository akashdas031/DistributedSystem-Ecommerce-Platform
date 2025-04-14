package CachingTest.Services;

import java.util.List;

import CachingTest.Entities.CacheUser;

public interface CacheUserService {

	CacheUser createUser(CacheUser cacheUser);
	List<CacheUser> getAllUsers();
	CacheUser getSingleUser(String userId);
	CacheUser updateUser(CacheUser cacheUser,String userId);
	void deleteUser(String userId);
}

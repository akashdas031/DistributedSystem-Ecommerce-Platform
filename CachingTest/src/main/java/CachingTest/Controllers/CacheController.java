package CachingTest.Controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import CachingTest.Entities.CacheUser;
import CachingTest.Services.CacheUserService;

@RestController
public class CacheController {

	private Logger logger=LoggerFactory.getLogger(CacheController.class);
	private CacheUserService cacheServ;
	
	public CacheController(CacheUserService cacheServ) {
		this.cacheServ=cacheServ;
	}
	
	@PostMapping("/createUser")
	public ResponseEntity<CacheUser> createUser(@RequestBody CacheUser cacheUser){
		CacheUser user = this.cacheServ.createUser(cacheUser);
		return new ResponseEntity<CacheUser>(user,HttpStatus.CREATED);
	}
	@GetMapping("/getAllUsers")
	public ResponseEntity<List<CacheUser>> getAllUser(){
		 List<CacheUser> allUsers = this.cacheServ.getAllUsers();
		 return new ResponseEntity<List<CacheUser>>(allUsers,HttpStatus.OK);
	}
	@GetMapping("/getSingleUser")
	public ResponseEntity<CacheUser> getSingleUser(@RequestParam("userId") String userId){
		 logger.info("User Id : "+userId);
		 CacheUser user = this.cacheServ.getSingleUser(userId);
		 logger.info("User : "+user);
		 return new ResponseEntity<CacheUser>(user,HttpStatus.OK);
	}
}

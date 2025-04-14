package CachingTest.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import CachingTest.Entities.CacheUser;

public interface CacheUserRepo extends JpaRepository<CacheUser, String>{

}

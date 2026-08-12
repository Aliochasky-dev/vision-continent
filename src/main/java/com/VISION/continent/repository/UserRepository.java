package com.VISION.continent.repository;



import com.VISION.continent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByTelephone(String telephone);



    Optional<User> findByGoogleId(String googleId);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
}

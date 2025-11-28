package com.deharri.ums.user;

import com.deharri.ums.user.entity.CoreUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<CoreUser, UUID> {

    Optional<CoreUser> findByUsername(String username);

    @Query("SELECT u.userId FROM CoreUser u WHERE u.username = ?1")
    Optional<UUID> getUuidByUsername(String username);
}

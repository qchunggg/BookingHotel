package com.hotelbooking.Repositories;

import com.hotelbooking.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    @Query("SELECT COUNT(u) > 0 FROM UserEntity u " +
            "WHERE LOWER(u.userName) = LOWER(:userName) " +
            "   OR LOWER(u.email) = LOWER(:email)")
    boolean existsDuplicate(@Param("userName") String userName,
                            @Param("email") String email);
}

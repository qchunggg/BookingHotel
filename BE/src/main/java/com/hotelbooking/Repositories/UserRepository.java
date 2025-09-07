package com.hotelbooking.Repositories;

import com.hotelbooking.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    @Query(
            "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
                    "FROM   UserEntity u " +
                    "WHERE  ( " +
                    "          LOWER(u.userName) = LOWER(:userName) " +
                    "       OR LOWER(u.email)    = LOWER(:email) " +
                    "       OR (:phone IS NOT NULL AND u.phone = :phone) " +
                    "      ) " +
                    "  AND  (:id IS NULL OR u.id <> :id)"
    )
    boolean existsDuplicate(@Param("userName") String userName,
                            @Param("email")    String email,
                            @Param("phone")    String phone,
                            @Param("id")       Long id);

    Optional<UserEntity> findByUserNameIgnoreCase(String userName);
}

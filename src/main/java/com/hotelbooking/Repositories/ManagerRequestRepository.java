package com.hotelbooking.Repositories;

import com.hotelbooking.Entities.ManagerRequestEntity;
import com.hotelbooking.Enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManagerRequestRepository extends JpaRepository<ManagerRequestEntity, Long>, JpaSpecificationExecutor<ManagerRequestEntity> {
    @Query(
            "SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
                    "FROM   ManagerRequestEntity m " +
                    "WHERE  m.user.id = :userId " +
                    "  AND  m.status  = :status"
    )
    boolean existsByUserIdAndStatus(@Param("userId") Long userId,
                                    @Param("status")  RequestStatus status);

    Page<ManagerRequestEntity> findAllByStatus(RequestStatus status, Pageable pageable);
}

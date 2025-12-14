package com.balakumaran.idempotent.idempotencycheck.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyRepositoryImpl
        extends JpaRepository<IdempotencyKeyEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select idem from IdempotencyKeyEntity idem where idem.key = :key")
    Optional<IdempotencyKeyEntity> lockByKey(@Param("key") String key);

}

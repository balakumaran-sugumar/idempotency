package com.balakumaran.idempotent.idempotencycheck.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.balakumaran.idempotent.idempotencycheck.repository.IdempotencyKeyEntity;
import com.balakumaran.idempotent.idempotencycheck.repository.IdempotencyRepositoryImpl;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepositoryImpl repo;

    @Transactional
    public Optional<String> getCompletedResponse(String key, String requestHash){

        repo.findById(key).orElseGet(() -> {

            IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
            entity.setKey(key);
            entity.setStatus(IdempotencyKeyEntity.Status.IN_PROGRESS);
            entity.setRequestHash(requestHash);
            try{
                return repo.saveAndFlush(entity);
            }catch (DataIntegrityViolationException exception){
                return null;
            }
        });

        //Lock the row only one thread at a time
        IdempotencyKeyEntity locked = repo.lockByKey(key)
                .orElseThrow(() -> new IllegalStateException("Row must exist"));

        //If already completed return the cached response
        if (locked.getStatus() == IdempotencyKeyEntity.Status.COMPLETED) {
            if (!Objects.equals(locked.getRequestHash(), requestHash)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Idempotency-Key reuse with different request");
            }
            return Optional.ofNullable(locked.getResponseJson());
        }

        // If in progress and hash differs conflict (Not blocking here)
        if (locked.getRequestHash() != null &&
                !Objects.equals(locked.getRequestHash(), requestHash)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Idempotency-Key reuse with different request");
        }

        return Optional.empty();
    }

    @Transactional
    public void completed(String key, String responseJson) {
        IdempotencyKeyEntity locked = repo.lockByKey(key)
                .orElseThrow(() -> new IllegalStateException("Row must exist"));
        locked.setStatus(IdempotencyKeyEntity.Status.COMPLETED);
        locked.setResponseJson(responseJson);
        repo.save(locked);
    }

    @Transactional
    public void failed(String key) {
        IdempotencyKeyEntity locked = repo.lockByKey(key)
                .orElseThrow(() -> new IllegalStateException("Row must exist"));
        locked.setStatus(IdempotencyKeyEntity.Status.FAILED);
        repo.save(locked);
    }

    public String doWork(){
        //implement business logic here
        return null;
    }




}

package com.balakumaran.idempotent.idempotencycheck.repository;


import com.balakumaran.idempotent.idempotencycheck.model.PaymentRequest;
import com.balakumaran.idempotent.idempotencycheck.service.IdempotencyService;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class IdempotentController {

    private final IdempotencyService idempotencyService;

    @PostMapping("/payments")
    public ResponseEntity<String> createPayments(@RequestHeader("Idempotency-Key") String idemKey,
                                                 @RequestBody PaymentRequest req){

        //Using Google Guava for 256 hashing
        String hashReq = Hashing.sha256()
                .hashString(req.toString(), StandardCharsets.UTF_8)
                .toString();

        //check for cachedInDB
        Optional<String> cachedInDB = idempotencyService.getCompletedResponse(idemKey, hashReq);
        if(cachedInDB.isPresent()){
            return ResponseEntity.ok(cachedInDB.get());
        }

        //Do the business logic
        try{
            String results = idempotencyService.doWork();
            //Mark the state as completed for the idempotent key
            idempotencyService.completed(idemKey, results);

            return ResponseEntity.ok(results);
        }catch (Exception ex){
            //if the transaction fails, mark the idempotent key as failed to be processed later by other threads
            idempotencyService.failed(idemKey);
            throw ex;
        }

    }


}

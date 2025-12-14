package com.balakumaran.idempotent.idempotencycheck.repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "idempotency_key")
@Getter @Setter
public class IdempotencyKeyEntity {

    @Id
    @Column(name="idem_key", length = 128)
    private String key;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "request_hash", length = 64)
    private String requestHash;

    @Column(name = "response_json", columnDefinition = "json")
    private String responseJson;

    public enum Status { IN_PROGRESS, COMPLETED, FAILED }

}

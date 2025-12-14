# Spring Boot Idempotency with MySQL

This project demonstrates how to implement idempotency in a distributed Spring Boot application using MySQL row-level locking and transactions.

## Key Concepts
- Idempotency keys
- MySQL `SELECT FOR UPDATE`
- Spring `@Transactional`
- Concurrency-safe request processing

## Use Cases
- Payments
- Wallets
- Account creation
- Financial workflows

## Tech Stack
- Java 17
- Spring Boot
- Spring Data JPA
- MySQL

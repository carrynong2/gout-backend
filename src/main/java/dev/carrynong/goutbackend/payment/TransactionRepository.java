package dev.carrynong.goutbackend.payment;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TransactionRepository extends CrudRepository<Transaction, Integer> {
    Optional<Transaction> findByIdempotentKey(String idempotentKey);
}

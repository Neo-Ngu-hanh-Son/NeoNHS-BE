package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findAll(Specification<Transaction> spec, Sort sort);

    Optional<Transaction> findByPaymentGateway(String paymentGateway);
}

package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.TransactionRepository;
import fpt.project.NeoNHS.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
}

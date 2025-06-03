package com.br.eduardozanela.statementextractor.model;

public record TransactionRecord(
    String transactionDate,
    String postedDate,
    String description,
    double amount
) {}
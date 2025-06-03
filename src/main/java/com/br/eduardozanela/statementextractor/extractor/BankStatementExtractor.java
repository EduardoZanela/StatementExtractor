package com.br.eduardozanela.statementextractor.extractor;

import java.util.List;

import com.br.eduardozanela.statementextractor.model.TransactionRecord;

public interface BankStatementExtractor {
    public List<TransactionRecord> extract(String data) throws Exception;
}
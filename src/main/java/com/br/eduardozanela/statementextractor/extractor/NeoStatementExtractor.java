package com.br.eduardozanela.statementextractor.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.eduardozanela.statementextractor.model.TransactionRecord;

public class NeoStatementExtractor implements BankStatementExtractor {
	
	private static Logger logger = LoggerFactory.getLogger(NeoStatementExtractor.class);
	
	@Override
	public List<TransactionRecord> extract(String data) {
			
		List<TransactionRecord> records = new ArrayList<>();

        String regex = "([A-Z][a-z]{2} \\d{1,2})\\s+([A-Z][a-z]{2} \\d{1,2})\\s+(.+?)\\s+([-+]?\\d+\\.\\d{2})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        boolean found = false;
        while (matcher.find()) {
            records.add(new TransactionRecord(
            		matcher.group(1), matcher.group(2), 
            		matcher.group(3).trim(), Double.parseDouble(matcher.group(4)) 
            ));
            found = true;
        }
        if (!found) {
            logger.warn("No records found for the statement ");
        }
                 
        return records;
	}	
}

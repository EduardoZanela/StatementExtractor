package com.br.eduardozanela.statementextractor.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.br.eduardozanela.statementextractor.model.TransactionRecord;

public class ATBStatementExtractor implements BankStatementExtractor {

	private static String extractPurchasesSection(String rawText) {
		String[] lines = rawText.split("\\R");
		StringBuilder section = new StringBuilder();
		boolean inPurchases = false;
		for(String line : lines) {
			if(line.contains("PURCHASES AND RETURNS")) {
				inPurchases = true;
			} else if(inPurchases && line.trim().startsWith("Total purchases")) {
				inPurchases = false;
			} else if(inPurchases && !line.trim().isEmpty()) {
				section.append(line.trim()).append("\n");
			}			
		}
		
		return section.toString();
	}
	
	@Override
	public List<TransactionRecord> extract(String data) throws Exception {
		List<TransactionRecord> records = new ArrayList<>();
        String purchaseSection = extractPurchasesSection(data);

        String regex = "([A-Z][a-z]{2} \\d{1,2})\\s+([A-Z][a-z]{2} \\d{1,2})\\s+(.+?)\\s+(\\d+\\.\\d{2})";
        
        Pattern pattern = Pattern.compile(regex);
        for (String line : purchaseSection.split("\\R")) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.find()) {
            	records.add(new TransactionRecord(
    		            matcher.group(1),  // transaction date
    		            matcher.group(2),  // posted date
    		            matcher.group(3).trim(),  // description
    		            Double.parseDouble(matcher.group(4))*-1 // value
    		        ));
            }
        }
		return records;
	}

}

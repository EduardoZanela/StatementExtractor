package com.br.eduardozanela.statementextractor.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.br.eduardozanela.statementextractor.model.TransactionRecord;

public class ATBChequingStatementExtractor implements BankStatementExtractor {

	private List<String> receivedTransactionDesc;
	
	public ATBChequingStatementExtractor() {
		receivedTransactionDesc = new ArrayList<String>();
		receivedTransactionDesc.add("INTERAC e-Transfer Received");
		receivedTransactionDesc.add("Direct Deposit");
		receivedTransactionDesc.add("Transfer From");
	}
	
	private static String extractPurchasesSection(String rawText) {
		String[] lines = rawText.split("\\R");
		StringBuilder section = new StringBuilder();
		boolean inPurchases = false;
		for(String line : lines) {
			if(line.contains("Details of your account transactions")) {
				inPurchases = true;
			} else if(inPurchases && line.trim().contains("Closing balance")) {
				inPurchases = false;
			} else if(inPurchases && !line.trim().isEmpty()) {
				section.append(line.trim()).append("\n");
			}			
		}
		
		return section.toString();
		
		
	}
	
	@Override
	public List<TransactionRecord> extract(String data) throws Exception {
		
		System.out.println(data);
		
		List<TransactionRecord> records = new ArrayList<>();
        String purchaseSection = extractPurchasesSection(data);

        String regex = "^([A-Za-z]{3} \\d{1,2}) (.+?) \\$([\\d,]+\\.\\d{2}) ([\\d,]+\\.\\d{2})$";
        
        Pattern pattern = Pattern.compile(regex);
        for (String line : purchaseSection.split("\\R")) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.find()) {
            	String desc = matcher.group(2).trim();
            	boolean isDeposit = receivedTransactionDesc.stream().anyMatch(desc::contains);
            	Double value = Double.parseDouble(matcher.group(3).replace(",", ""));
            	value = isDeposit ? value : value * -1;
            	
            	records.add(new TransactionRecord(
    		            matcher.group(1),  // transaction date
    		            matcher.group(1),  // posted date
    		            desc,  // description
    		            value // value
    		        ));
            } else if(records.size() > 0) {
            	var transaction = records.removeLast();
            	
            	String desc = transaction.description() + " " + line;
            	boolean isDeposit = receivedTransactionDesc.stream().anyMatch(desc::contains);
            	Double value = isDeposit ? Math.abs(transaction.amount()) : Math.abs(transaction.amount()) * -1;
            	records.add(new TransactionRecord(
    		            transaction.transactionDate(),  // transaction date
    		            transaction.postedDate(),  // posted date
    		            desc,  // description
    		            value // value
    		        ));            	
            }
        }
		return records;
	}

}

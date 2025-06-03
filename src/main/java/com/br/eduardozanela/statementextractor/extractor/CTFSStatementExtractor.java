package com.br.eduardozanela.statementextractor.extractor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.br.eduardozanela.statementextractor.model.TransactionRecord;

public class CTFSStatementExtractor implements BankStatementExtractor {

	private static String extractPurchasesSection(String rawText) {
		String[] lines = rawText.split("\\R");
		StringBuilder section = new StringBuilder();
		boolean inPurchases = false;
		
		for(String line : lines) {
			if(line.contains("Purchases - Card #")) {
				inPurchases = true;
			} else if(inPurchases && line.trim().startsWith("Total purchases for")) {
				inPurchases = false;
			} else if(inPurchases && !line.trim().isEmpty()) {
				section.append(line.trim()).append("\n");
			}			
		}
		
		return section.toString();
	}
	
	private static String normalizeLineBreaks(String text) {
	    return text.replaceAll("(?<!\\n)(?<!\\n[A-Z][a-z]{2} \\d{1,2})\\n(?![A-Z][a-z]{2} \\d{1,2})", " ");
	}
	
	 public static String extractStatementDate(String text) {
        Pattern pattern = Pattern.compile("Statement date: ([A-Za-z]+ \\d{1,2}, \\d{4})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String rawDate = matcher.group(1); // e.g., "April 27, 2025"

            // Convert to LocalDate
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            LocalDate date = LocalDate.parse(rawDate, inputFormat);

            // Output as "Apr 27"
            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("MMM d");
            return date.format(outputFormat);
        }

        return null; // or throw exception if required
    }

	public static List<TransactionRecord> extractInstallmentPlans(String rawText) {
	    List<TransactionRecord> plans = new ArrayList<>();

	    Pattern rowPattern = Pattern.compile("(?m)^([A-Z][a-z]{2} \\d{1,2} \\d{4})\\s+\\$?(\\d+\\.\\d{2})\\s+\\$?(\\d+\\.\\d{2})\\s+\\$?(\\d+\\.\\d{2})\\s+([A-Z][a-z]{2} \\d{1,2} \\d{4})$");

        String formattedDate = extractStatementDate(rawText);
	    
	    boolean inSection = false;
	    for (String line : rawText.split("\\R")) {
	        if (line.contains("Details of your 24 Equal Payments Plan")) {
	            inSection = true;
	            continue;
	        }
	        if (!inSection) continue;
	        Matcher matcher = rowPattern.matcher(line.trim());
	        if (matcher.find()) {
	            plans.add(new TransactionRecord(
	            	formattedDate,
	            	formattedDate,
	            	"Plan Start: " + matcher.group(1) + " Original Amount: " + matcher.group(2) + " Balance: " + matcher.group(4) + " Expiry date: " + matcher.group(5),
	                Double.parseDouble(matcher.group(3))*-1
	            ));
	        }
	    }

	    return plans;
	}
	
	@Override
	public List<TransactionRecord> extract(String data) throws Exception {
		String section = extractPurchasesSection(data); 
	    String cleaned = normalizeLineBreaks(section).trim();
	    String[] lines = cleaned.split("\\R"); // splits by newlines
	    
	    Pattern pattern = Pattern.compile("([A-Z][a-z]{2} \\d{1,2})\\s+([A-Z][a-z]{2} \\d{1,2})\\s+(.+?)\\s+(\\d+\\.\\d{2})$");
	    List<TransactionRecord> records = new ArrayList<>();
	    for(String line : lines) {
	    	
	        Matcher matcher = pattern.matcher(line.trim());
	        if(matcher.find()) {
		        records.add(new TransactionRecord(
		            matcher.group(1),  // transaction date
		            matcher.group(2),  // posted date
		            matcher.group(3).trim(),  // description
		            Double.parseDouble(matcher.group(4))*-1 // value
		        ));
	        }
	    }
	    records.addAll(extractInstallmentPlans(data));
		return records;
	}

}

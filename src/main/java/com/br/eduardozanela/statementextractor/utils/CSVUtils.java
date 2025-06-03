package com.br.eduardozanela.statementextractor.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.eduardozanela.statementextractor.StatmentExtractorApplication.Bank;
import com.br.eduardozanela.statementextractor.model.TransactionRecord;

public class CSVUtils {
	
	private static Logger logger = LoggerFactory.getLogger(CSVUtils.class);

	public static void exportToCustomCsv(List<TransactionRecord> transactions, String filePath, Bank bank) throws IOException {
        
		FileWriter writer = new FileWriter(filePath);
        writer.write("Date,Amount,Category,Title,Note,Account\n");

        // Define a date formatter to match "Mar 29" format to "2025-05-29"
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy MMM d");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        
    	// Get current year (you can change it if necessary)
        int currentYear = LocalDate.now().getYear();
        
        for (TransactionRecord record : transactions) {
        	
            // Parse the endDate (e.g., "Mar 29") and convert it to full date with year
            String endDate = record.postedDate(); // e.g., "Mar 29"
            LocalDate date = LocalDate.parse(currentYear + " " + endDate, inputFormatter);

            // Format the date as desired
            String formattedDate = date.atStartOfDay().format(outputFormatter);
        	
            writer.write(String.format("%s,%.2f,,\"%s\",,%s\n",
            		formattedDate,
                    record.amount(),
                    record.description(),
                    bank.toString()
            ));
        }
        
        logger.info("CSV generated at " + filePath);
        
        writer.close();
    }
}

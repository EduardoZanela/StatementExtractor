package com.br.eduardozanela.statementextractor;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.br.eduardozanela.statementextractor.extractor.ATBChequingStatementExtractor;
import com.br.eduardozanela.statementextractor.extractor.ATBStatementExtractor;
import com.br.eduardozanela.statementextractor.extractor.BankStatementExtractor;
import com.br.eduardozanela.statementextractor.extractor.CTFSStatementExtractor;
import com.br.eduardozanela.statementextractor.extractor.NeoStatementExtractor;
import com.br.eduardozanela.statementextractor.model.TransactionRecord;
import com.br.eduardozanela.statementextractor.utils.CSVUtils;

import net.sourceforge.tess4j.Tesseract;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "StatementExtractor", mixinStandardHelpOptions = true, version = "1.0", description = "Parses band statment PDFs and extracts transactions to CSV")
public class StatmentExtractorApplication implements Callable<Integer> {
    
	private static Logger logger = LoggerFactory.getLogger(StatmentExtractorApplication.class);
	
	private BankStatementExtractor extractor;
	
	public enum Bank {
	    neo,
	    ctfs,
	    atb,
	    atbchequing
	}	
	
	@Option(names = {"-f", "--file"}, description = "Path to the PDF statement file", required = true)
	private List<String> filePaths;
	
	@Option(names = {"-b", "--bank"}, description = "Bank name. Allowed values: ${COMPLETION-CANDIDATES}", required = true)
	private Bank bank;
	
	@Option(names = {"-o", "--output"}, description = "Path to the CSV output", defaultValue = "transactions.csv")
	private String outputPath;
	
	@Override
	public Integer call() throws Exception {
		System.out.println("File path: " + filePaths);
        System.out.println("Bank name: " + bank);
        
        switch(bank)
        {
			case atb:
				extractor = new ATBStatementExtractor();
				break;
			case atbchequing:
				extractor = new ATBChequingStatementExtractor();
				break;
			case ctfs:
				extractor = new CTFSStatementExtractor();
				break;
			case neo:
				extractor = new NeoStatementExtractor(); 
				break;
        }
        
		InputStream is;
        for (String fileName : filePaths) {
            is = new FileInputStream(fileName);
            String data;
            if( bank == Bank.atb || bank == Bank.atbchequing) {
            	data = extractTextWithOcr(is);
            } else {
            	data = extractTextEncrypted(is);
            }
            try {
            	List<TransactionRecord> records = extractor.extract(data);
            	System.out.println(records);
            	if(!records.isEmpty()) {
            		CSVUtils.exportToCustomCsv(records, outputPath, bank);
            	}
            } catch( Exception ex ) {
            	logger.error( ex.getMessage() );
            	System.out.println(ex.getMessage());
            	return 1;
            }
            is.close();
        }  
        
        
        return 0;
	}

	public static String extractTextWithOcr(InputStream inputStream) {
        StringBuilder fullText = new StringBuilder();

        try (PDDocument document = PDDocument.load(inputStream)) {
            if (document.isEncrypted()) {
                document.setAllSecurityToBeRemoved(true);
            }

            PDFRenderer renderer = new PDFRenderer(document);
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
            tesseract.setLanguage("eng");

            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage image = renderer.renderImageWithDPI(page, 300);
                String result = tesseract.doOCR(image);
                fullText.append(result).append("\n\n");
            }

        } catch (Exception e) {
            throw new RuntimeException("OCR failed: " + e.getMessage(), e);
        }

        return fullText.toString();
    }
	
	public static String extractTextEncrypted(InputStream pdfInputStream) {
        try (PDDocument document = PDDocument.load(pdfInputStream)) {
            if (document.isEncrypted()) {
                System.out.println("Encrypted PDF detected. Removing security...");
                document.setAllSecurityToBeRemoved(true); // ⬅️ this bypasses content restrictions
            }

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract PDF text: " + e.getMessage(), e);
        }
    }
	
    public static void main(String... args) throws IOException, SAXException {
    	int exitCode = new CommandLine(new StatmentExtractorApplication()).execute(args);
    	System.exit(exitCode);   
    }
}

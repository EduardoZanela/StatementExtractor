# 📎 StatementExtractor

**StatementExtractor** is a CLI and API tool that parses Canadian bank statement PDFs into structured data (CSV or JSON), using smart categorization and OCR support. It's designed to help users track and categorize personal expenses easily and accurately.

---

## 🚀 Features

* 📄 Extracts transactions from PDF statements
* 🏦 Supports multiple banks with bank-specific parsers
* 🧠 Auto-categorizes expenses based on past behavior (planned)
* 📝 CLI prompts for manual category corrections (learning mode) (planned)
* 🔍 OCR fallback for scanned (image-based) PDFs using Tesseract
* 👷 Designed for easy integration with a future API and frontend (planned)

---

## 🏦 Supported Banks

| Bank                              | Features                    |
| --------------------------------- | --------------------------- |
| **CTFS** (Canadian Tire)          | OCR & rule-based parsing    |
| **ATB** (Alberta Treasury Branch) | Full transaction extraction |
| **Neo**                           | (coming soon)               |

---

## 🛠 Requirements

* Java 17 or later
* [Tesseract OCR 5.5.0+](https://github.com/tesseract-ocr/tesseract/releases/tag/5.5.0) (installed and on your system path)

> ℹ️ On Windows: install from the release above and ensure `tesseract.exe` is accessible, e.g., in `C:\Program Files\Tesseract-OCR\`

---

## 🧑‍💻 How to Compile

Use [Maven](https://maven.apache.org/) to build:

```bash
git clone https://github.com/EduardoZanela/StatementExtractor.git
cd StatementExtractor
mvn clean install
```

This builds:

* A CLI executable JAR under `cli/target/`
* An API-ready Spring Boot JAR under `api/target/`

---

## 💻 How to Run (CLI Mode)

```bash
java -jar cli/target/cli-1.0-jar-with-dependencies.jar \
  --file path/to/statement.pdf \
  --bank ctfs
```

You’ll be prompted to confirm or correct categories, and a CSV will be generated with the results. (planned)

---

## 🌐 API Mode (Coming Soon)

The API version can be run via:

```bash
java -jar api/target/api-1.0.jar
```

It will expose REST endpoints for uploading and parsing statements.

---

## 🧠 Categorization Logic

* 💡 Learns from your past input (stored in `categories.csv`) (planned)
* 🧠 Matches descriptions based on keywords (planned)
* 📜 Falls back to ChatGPT or rules for new entries (planned)

---

## 🧪 Example CSV Output

```csv
Date,Amount,Category,Title,Note,Account
2025-05-10,-50,Groceries,Fruits and Vegetables,,Neo
2025-05-11,-18.78,Subscriptions,SPOTIFY STOCKHOLM,,Neo
```

---

## 📂 Project Structure

```
StatementExtractor/
├── core/   → parsing logic, models, OCR
├── cli/    → Picocli-based command line interface
├── api/    → Spring Boot REST API (in progress)
```

---

## 📃 License

MIT License

---

## 🤝 Contributions

PRs, issue reports, and bank format contributions are welcome!

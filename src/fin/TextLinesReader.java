package fin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextLinesReader {

	private String currentLine;
	private List<Transaction> transactions = new ArrayList<Transaction>();
	private Transaction currentTransaction;
	private File file;
	public static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy");
	private static Pattern firstLinePattern = Pattern
			.compile("[0-9][0-9][.][0-9][0-9][.][0-9][0-9][0-9][0-9]");
	private static Pattern lastLinePattern = Pattern
			.compile("(([[0-9][.]])*[,][0-9][0-9])[+-]");
	public static DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
			.getInstance(new Locale("de"));

	public TextLinesReader(File file) {
		this.file = file;
	}

	public void process() throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			currentLine = br.readLine();

			while (currentLine != null) {
				currentLine = currentLine.trim();
				if (currentLine.length() > 0) {
					processLine();
				}
				currentLine = br.readLine();
			}
		} finally {
			br.close();
		}
	}

	private void processLine() throws Exception {
		if (currentTransaction == null) {
			findFirstLine();
		} else {
			appendNextLine();
		}
	}

	private void appendNextLine() throws Exception {

		Matcher m = lastLinePattern.matcher(currentLine);
		if (m.matches()) {
			String amountString;
			if (currentLine.endsWith("-")) {
				amountString = "-" + currentLine.replace("-", "");
			} else {
				amountString = currentLine.replace("+", "");
			}

			Number amountNumber = decimalFormat.parse(amountString);
			currentTransaction.setAmount(amountNumber);

			completeTransaction();
			transactions.add(currentTransaction);
			currentTransaction = null;
		} else {
			currentTransaction.setText(currentTransaction.getText() + " "
					+ currentLine);
		}
	}

	private void completeTransaction() {
		for (Categorie cat : Categorie.values()) {
			Matcher matcher = cat.getPattern().matcher(
					currentTransaction.getText().toUpperCase());
			if (matcher.matches()) {
				currentTransaction.setCategorie(cat);
				return;
			}
		}
	}

	private void findFirstLine() throws ParseException {
		if (currentTransaction != null)
			throw new RuntimeException("Transaction still open");

		Matcher m = firstLinePattern.matcher(currentLine);
		if (!m.matches()) {
			throw new RuntimeException("Date expected in " + currentLine);
		}
		currentTransaction = new Transaction();
		Date date = dateFormat.parse(currentLine);
		currentTransaction.setDate(date);
		currentTransaction.setText("");
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}
}

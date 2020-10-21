package fin;

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

public class MlpLinesReader {

	private String[] lines;
	private String currentLine;
	private List<Transaction> transactions = new ArrayList<Transaction>();
	private String year;
	private Transaction currentTransaction;
	public static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy");
	private static Pattern firstLinePattern = Pattern
			.compile("([0-9][0-9][.][0-9][0-9][.])[ ]*[[0-9][.]]*[ ](.*)[ ]([[0-9],.]*) (H|S)[ ]*");
	private static Pattern yearLinePattern = Pattern
	.compile(".*[ ]1?[0-9][/](20[0-9][0-9]).*");
	public static DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
			.getInstance(new Locale("de"));
	public static DecimalFormat decimalFormatOut = new DecimalFormat("0.00");
	public MlpLinesReader(String text) {
		this.lines = text.split("\r\n");
	}

	public void process() throws Exception {
		for (int i = 0; i < lines.length; i++) {
			currentLine = lines[i];
			processLine();
		}

	}

	private void processLine() throws Exception {
		findYear();
		if (year == null)
			return;
		if (currentTransaction == null) {
			findFirstLine();
			if (currentTransaction == null)
				return;
		} else {
			appendNextLine();
		}
	}

	private void appendNextLine() throws Exception {
		if (currentLine.startsWith("              ") && !currentLine.startsWith("               ")) {
			currentTransaction.setText(currentTransaction.getText() + " "
					+ currentLine.trim());
		} else {
			completeTransaction();
			transactions.add(currentTransaction);
			currentTransaction = null;
			findFirstLine();

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
			return;

		if (currentLine.length() > 88) {
			Matcher m = firstLinePattern.matcher(currentLine);
			if (!m.matches())
				return;
			currentTransaction = new Transaction();

			Date date = dateFormat.parse(m.group(1) + year);
			currentTransaction.setDate(date);

			String HS = m.group(4);
			String amountString = m.group(3);
			if (HS.equals("S"))
				amountString = "-" + amountString;

			String text = m.group(2).trim();

			Number amountNumber = decimalFormat.parse(amountString);
			currentTransaction.setAmount(amountNumber);

			currentTransaction.setText(text);

		}
	}

	private void findYear() {
		if (year != null)
			return;
		Matcher matcher = yearLinePattern.matcher(currentLine);
		
		if (matcher.matches()) {
			year = matcher.group(1);
		}
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}
}

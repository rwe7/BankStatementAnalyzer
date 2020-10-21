package fin;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.ScratchFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfReader {

	private static Date START_DATE;
	private static Date END_DATE;
	private static int MONTHS;
	private List<Transaction> transactions = new ArrayList<Transaction>();

	/**
	 * @param args
	 * @throws Exception
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException, Exception {

		System.err.close();
		START_DATE = MlpLinesReader.dateFormat.parse("01.01.2019");
		END_DATE = MlpLinesReader.dateFormat.parse("31.12.2020");
		MONTHS = 1;
		String folder = "C:\\Users\\ASUS\\Google Drive\\Rechnungen_Finanzen\\Kontoauszuege";
		PdfReader pdfReader = new PdfReader();
		try {
			pdfReader.processFolder(folder);
			pdfReader.print();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private void print() {
		sort();

		for (Categorie cat : Categorie.values()) {
			System.out.println("");
			float sum = 0;
			for (Transaction t : transactions) {
				if (t.getCategorie() == cat) {
					System.out.println(t);
					sum += t.getAmount().floatValue();
				}
			}
			System.out.println("---------------");
			System.out.println(MlpLinesReader.decimalFormatOut.format(sum / MONTHS));

		}
		System.out.println("");
		System.out.println("");

		for (Group group : Group.values()) {
			System.out.println("");
			System.out.println("========== " + group + " ============");
			float groupSum = 0;
			for (Categorie cat : Categorie.values())
				if (cat.getGroup() == group) {
					float sum = 0;
					for (Transaction t : transactions) {
						if (t.getCategorie() == cat) {
							sum += t.getAmount().floatValue();
							groupSum += t.getAmount().floatValue();
						}
					}

					String average = MlpLinesReader.decimalFormatOut.format(sum / MONTHS);
					System.out.println(cat + ": " + average);
				}
			String groupAverage = MlpLinesReader.decimalFormatOut.format(groupSum / MONTHS);
			System.out.println("----------------");
			System.out.println(groupAverage);

		}

	}

	private void sort() {
		Collections.sort(transactions, new Comparator<Transaction>() {

			@Override
			public int compare(Transaction object1, Transaction object2) {
				// TODO Auto-generated method stub
				return object1.getDate().compareTo(object2.getDate());
			}
		});
	}

	private void processFolder(String sDir) throws Exception {
		File[] faFiles = new File(sDir).listFiles();
		for (File file : faFiles) {
			if (file.getName().matches("^(.*?)")) {
				if (file.getName().matches(".*Kontoauszug.*[.](pdf|txt)")) {
					if (true || file.getName().equals("Kontoauszug_4010732402__Nr.013_vom_04.01.2012.pdf")) {
						process(file);
					}
				}
			}
			if (file.isDirectory()) {
				processFolder(file.getAbsolutePath());
			}
		}
	}

	private void process(File file) throws Exception {

		if ((file.getAbsolutePath().indexOf("Commerzbank") > 0)
				|| (file.getAbsolutePath().indexOf("Kreditkarte")) > 0) {

			if (file.getName().endsWith("txt")) {

				TextLinesReader linesReader = new TextLinesReader(file);
				linesReader.process();
				System.out.println(linesReader.getTransactions().size() + " transactions in " + file.getName());
				for (Transaction transaction : linesReader.getTransactions()) {
					if (transaction.getDate().after(START_DATE) && transaction.getDate().before(END_DATE)) {
						transaction.setFile(file.getName());
						transactions.add(transaction);
					}
				}
			}
		} else {
			if (file.getName().endsWith("pdf")) {

				ScratchFile scratchFile = new ScratchFile(new File(System.getProperty("java.io.tmpdir")));

				RandomAccessRead source = scratchFile.createBuffer(new FileInputStream(file));
				PDFParser parser = new PDFParser(source);
				parser.parse();
				PDDocument pdDoc = parser.getPDDocument();

				PDFTextStripper stripper = new PDFTextStripper();
				String text = stripper.getText(pdDoc);
				MlpLinesReader linesReader = new MlpLinesReader(text);
				linesReader.process();
				System.out.println(linesReader.getTransactions().size() + " transactions in " + file.getName());
				for (Transaction transaction : linesReader.getTransactions()) {
					if (transaction.getDate().after(START_DATE) && transaction.getDate().before(END_DATE)) {
						transaction.setFile(file.getName());
						transactions.add(transaction);
					}
				}
				pdDoc.close();
			}
		}
	}

}

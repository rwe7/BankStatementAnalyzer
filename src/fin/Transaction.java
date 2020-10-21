package fin;

import java.math.BigDecimal;
import java.util.Date;

public class Transaction {
	
	private Date date;
	private Number amount;
	private String text;
	private Categorie categorie;
	private String file;
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(MlpLinesReader.dateFormat.format(date));
		buffer.append(" (");
		buffer.append(categorie);
		buffer.append(") ");
		String format = MlpLinesReader.decimalFormatOut.format(amount);
		buffer.append("          ".substring(format.length()) + format);
		buffer.append(" ");
		buffer.append(text);
		buffer.append(" (");
		buffer.append(file);
		buffer.append(")");
		return buffer.toString();	
	}
	
	
	public String getFile() {
		return file;
	}


	public void setFile(String file) {
		this.file = file;
	}


	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Number getAmount() {
		return amount;
	}
	public void setAmount(Number amount) {
		this.amount = amount;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Categorie getCategorie() {
		return categorie;
	}
	public void setCategorie(Categorie categorie) {
		this.categorie = categorie;
	}	

}

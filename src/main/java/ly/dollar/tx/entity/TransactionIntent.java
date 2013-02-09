package ly.dollar.tx.entity;

import java.math.BigDecimal;

public class TransactionIntent {
	private String mention;
	private String hashtag;
	private BigDecimal amount;
	private String dollarlyMention;
	private String mode;
	public String getMention() {
		return mention;
	}
	public void setMention(String mention) {
		this.mention = mention;
	}
	public String getHashtag() {
		return hashtag;
	}
	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getDollarlyMention() {
		return dollarlyMention;
	}
	public void setDollarlyMention(String dollarlyMention) {
		this.dollarlyMention = dollarlyMention;
	}
	
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public boolean matched()
	{
		return this.hashtag != null 
				&& this.amount != null
				&& this.mention != null
				&& this.dollarlyMention != null;
	}

}

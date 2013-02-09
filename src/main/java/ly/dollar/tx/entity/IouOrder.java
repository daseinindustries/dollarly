package ly.dollar.tx.entity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@SuppressWarnings("deprecation") @JsonWriteNullProperties(value=false)
public class IouOrder extends ExtSystemEntity implements Serializable {
	
private static final long serialVersionUID = 1L;
	
	public enum Status
	{
		OPEN, PENDING_CONFIRMATION, PENDING_PAYMENT, VOID, PAID, FAILED
	}
	
	public enum FailReason
	{
		 FAILED_PAYMENT, UNCONFIRMABLE, ERROR
	}
	
	@Id
	private String id;
	private String mention;
	private String dollarlyMention;
	private String hashtag;
	private String payerUserId;
	private String payeeUserId;
	private String payeeFundingStatus;
	private String payerFundingStatus;
	private String payeeHandle;
	private String payerHandle;

	private BigDecimal amount;

	private Status status = Status.OPEN;
	private Date statusLastModifiedOn = new Date();
	private FailReason failReason;
	
	public IouOrder(){}
	public IouOrder(TransactionIntent tx){
	 this.setAmount(tx.getAmount());
	 this.setMention(tx.getMention());
	 this.setDollarlyMention(tx.getDollarlyMention());
	 this.setHashtag(tx.getHashtag());
	}
	
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getPayerUserId()
	{
		return payerUserId;
	}

	public void setPayerUserId(String payerUserId)
	{
		this.payerUserId = payerUserId;
	}

	public String getPayeeUserId()
	{
		return payeeUserId;
	}

	public void setPayeeUserId(String payeeUserId)
	{
		this.payeeUserId = payeeUserId;
	}

	public String getMention()
	{
		return mention;
	}

	public void setMention(String mention)
	{
		this.mention = mention;
	}

	public String getHashtag()
	{
		return hashtag;
	}

	public void setHashtag(String hashtag)
	{
		this.hashtag = hashtag;
	}


	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public Date getStatusLastModifiedOn()
	{
		return statusLastModifiedOn;
	}

	public void setStatusLastModifiedOn(Date statusLastModifiedOn)
	{
		this.statusLastModifiedOn = statusLastModifiedOn;
	}

	public FailReason getFailReason()
	{
		return failReason;
	}

	public void setFailReason(FailReason failReason)
	{
		this.failReason = failReason;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getPayeeFundingStatus() {
		return payeeFundingStatus;
	}
	public void setPayeeFundingStatus(String payeeFundingStatus) {
		this.payeeFundingStatus = payeeFundingStatus;
	}
	public String getPayerFundingStatus() {
		return payerFundingStatus;
	}
	public void setPayerFundingStatus(String payerFundingStatus) {
		this.payerFundingStatus = payerFundingStatus;
	}
	public String getPayeeHandle() {
		return payeeHandle;
	}
	public void setPayeeHandle(String payeeHandle) {
		this.payeeHandle = payeeHandle;
	}
	public String getPayerHandle() {
		return payerHandle;
	}
	public void setPayerHandle(String payerHandle) {
		this.payerHandle = payerHandle;
	}
	public void fail(FailReason reason) 
	{
		setStatus(Status.FAILED);
		setStatusLastModifiedOn(new Date());
		setFailReason(reason);
	}
	
	public void succeed() 
	{
		setStatus(Status.PAID);
		setStatusLastModifiedOn(new Date());
	}
	
	public boolean matched()
	{
		return this.hashtag != null 
				&& this.amount != null
				&& this.mention != null
				&& this.dollarlyMention != null;
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

	public String getDollarlyMention() {
		return dollarlyMention;
	}

	public void setDollarlyMention(String dollarlyMention) {
		this.dollarlyMention = dollarlyMention;
	}

	
}

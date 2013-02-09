package ly.dollar.tx.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@SuppressWarnings("deprecation") @JsonWriteNullProperties(value=false)
@JsonIgnoreProperties({ "id" })
public class PurchaseOrder extends ExtSystemEntity implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public enum Status
	{
		OPEN, PAID, FAILED
	}
	
	public enum FailReason
	{
		LACK_OF_INVENTORY, FAILED_PAYMENT, UNCONFIRMABLE, ERROR
	}

	@Id
	private String id;
	private String mention;
	private String hashtag;
	private String listingId;
	private String payerUserId;
	private String payeeUserId;
	private BigDecimal pricePerUnit;
	private Long numUnits = 1L;
	private Status status = Status.OPEN;
	private Date statusLastModifiedOn = new Date();
	private FailReason failReason;

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

	public String getListingId()
	{
		return listingId;
	}

	public void setListingId(String listingId)
	{
		this.listingId = listingId;
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

	public BigDecimal getPricePerUnit()
	{
		return pricePerUnit;
	}

	public void setPricePerUnit(BigDecimal price)
	{
		this.pricePerUnit = price;
	}

	public Long getNumUnits()
	{
		return numUnits;
	}

	public void setNumUnits(Long numUnits)
	{
		this.numUnits = numUnits;
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
				&& this.pricePerUnit != null
				&& this.mention != null;
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}


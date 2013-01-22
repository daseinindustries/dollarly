package ly.dollar.tx.entity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@SuppressWarnings("deprecation") @JsonWriteNullProperties(value=false)
@JsonIgnoreProperties({ "id" })
public class Payment extends ExtSystemEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum Status
	{
		UNPROCESSED, PROCESSING, PROCESSED, FAILED, ABORTED
	}

	@Id
	private String id;
	private BigDecimal amount;
	private String listingId;
	private String payerUserId;
	private String payeeUserId;
	private String purchaseOrderId;
	private Date statusLastModifiedOn = new Date();
	private Status status = Status.UNPROCESSED;
	@Transient
	private Long timeToCompletion;

	public Payment()
	{
	}

	public Payment(IouOrder i)
	{
		this.payeeUserId = i.getPayeeUserId();
		this.payerUserId = i.getPayerUserId();
		this.purchaseOrderId = i.getId();
		//this.listingId = p.getListingId();
		//BigDecimal numUnits = new BigDecimal(p.getNumUnits());
		this.amount = i.getAmount();
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}

	public String getListingId()
	{
		return listingId;
	}

	public void setListingId(String listingId)
	{
		this.listingId = listingId;
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

	public String getPurchaseOrderId()
	{
		return purchaseOrderId;
	}

	public void setPurchaseOrderId(String purchaseOrderId)
	{
		this.purchaseOrderId = purchaseOrderId;
	}

	public Date getStatusLastModifiedOn()
	{
		return statusLastModifiedOn;
	}

	public void setStatusLastModifiedOn(Date statusLastModifiedOn)
	{
		this.statusLastModifiedOn = statusLastModifiedOn;
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public Long getTimeToCompletion()
	{
		return timeToCompletion;
	}

	public void setTimeToCompletion(Long timeToCompletion)
	{
		this.timeToCompletion = timeToCompletion;
	}

	public void fail(String notes)
	{
		this.extSystemNotes = notes;
		complete(Status.FAILED);
	}

	public void abort()
	{
		complete(Status.ABORTED);
	}

	public void succeed(String notes)
	{
		this.extSystemNotes = notes;
		complete(Status.PROCESSED);
	}

	private void complete(Status status)
	{
		this.status = status;
		this.statusLastModifiedOn = new Date();
		this.timeToCompletion = statusLastModifiedOn.getTime()
				- createdOn.getTime();
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}
}

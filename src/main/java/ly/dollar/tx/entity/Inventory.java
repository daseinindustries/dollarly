package ly.dollar.tx.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@JsonIgnoreProperties({ "id" })
public class Inventory implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id 
	private String id;
	private String listingId;
	private Long quantity;

	public Inventory()
	{
	}
	
	public Inventory(String listingId, Long quantity)
	{
		this.listingId = listingId;
		this.quantity = quantity;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getListingId()
	{
		return listingId;
	}

	public void setListingId(String listingId)
	{
		this.listingId = listingId;
	}

	public Long getQuantity()
	{
		return quantity;
	}

	public void setQuantity(Long quantity)
	{
		this.quantity = quantity;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}

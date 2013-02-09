package ly.dollar.tx.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@SuppressWarnings("deprecation") @JsonWriteNullProperties(value=false)
public class LedgerTotals implements Serializable{
	

	private static final long serialVersionUID = 1L;
	@Id
	private String id;
	private String userId;
	private Long phone;
	private BigDecimal openCollects = new BigDecimal(0);
	private BigDecimal openPays = new BigDecimal(0);
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Long getPhone() {
		return phone;
	}
	public void setPhone(Long phone) {
		this.phone = phone;
	}
	public BigDecimal getOpenCollects() {
		return openCollects;
	}
	public void setOpenCollects(BigDecimal openCollects) {
		this.openCollects = openCollects;
	}
	public BigDecimal getOpenPays() {
		return openPays;
	}
	public void setOpenPays(BigDecimal openPays) {
		this.openPays = openPays;
	}
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}
}

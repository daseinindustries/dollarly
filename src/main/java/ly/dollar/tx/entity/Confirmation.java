package ly.dollar.tx.entity;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@SuppressWarnings("deprecation") @JsonWriteNullProperties(value=false)
public class Confirmation extends ExtSystemEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public enum Status
	{
		REQUESTED, DELIVERED, REPLIED, EXPIRED, UNDELIVERABLE, VOID
	}
	
	
	
	
	@Id
	private String id;
	private String orderId;
	private String payerUserId;
	private String payeeUserId;
	private Long payeePhone;
	private Long phone;
	private String message;
	private Integer code;
	private Status status = Status.REQUESTED;
	private Date statusLastModifiedOn = new Date();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Confirmation() {
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getPayerUserId() {
		return payerUserId;
	}
	public void setPayerUserId(String payerUserId) {
		this.payerUserId = payerUserId;
	}
	public String getPayeeUserId() {
		return payeeUserId;
	}
	public void setPayeeUserId(String payeeUserId) {
		this.payeeUserId = payeeUserId;
	}


	public Long getPayeePhone() {
		return payeePhone;
	}
	public void setPayeePhone(Long payeePhone) {
		this.payeePhone = payeePhone;
	}
	public Long getPhone() {
		return phone;
	}
	public void setPhone(Long phone) {
		this.phone = phone;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public Date getStatusLastModifiedOn() {
		return statusLastModifiedOn;
	}
	public void setStatusLastModifiedOn(Date statusLastModifiedOn) {
		this.statusLastModifiedOn = statusLastModifiedOn;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public void setMessage(String body) {
		this.message = body;
	}
	public String getMessage() {
		return message;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}
	

}

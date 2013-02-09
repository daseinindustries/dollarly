package ly.dollar.tx.entity;

import java.io.Serializable;
import java.util.Date;


import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;

public class PhoneConfirmation extends ExtSystemEntity implements Serializable {
	
private static final long serialVersionUID = 1L;
	
	public enum Status
	{
		REQUESTED, DELIVERED, REPLIED, CONFIRMED, EXPIRED, UNDELIVERABLE, INVALID
	}
	
	@Id
	private String id;
	private String userId;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
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
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}

}

package ly.dollar.tx.entity;

import java.io.Serializable;
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
public class Sms extends ExtSystemEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;
	
	private String senderPhone;
	private String dollarlyPhone;
	private String receiverPhone;
	private String threadId;
	private String dts;
	private String message;
	private String status;
	private String receiverUserName;
	public Sms(){}
	
	public Sms(SmsPostRequest spr){
		this.senderPhone = spr.getSenderPhone();
		this.dollarlyPhone = spr.getDollarlyPhone();
		this.receiverPhone = spr.getReceiverPhone();
		this.receiverUserName = spr.getReceiverUserName();
		this.threadId = spr.getTid();
		this.dts = spr.getDts();
		this.message= spr.getMessage();
		this.extSystem = ExtSystem.SMS;
		this.extSystemId = spr.getId();
		this.extSystemDate = new Date();
		this.status = "UNPROCESSED";
		
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSenderPhone() {
		return senderPhone;
	}
	public void setSenderPhone(String senderPhone) {
		this.senderPhone = senderPhone;
	}
	public String getDollarlyPhone() {
		return dollarlyPhone;
	}
	public void setDollarlyPhone(String dollarlyPhone) {
		this.dollarlyPhone = dollarlyPhone;
	}
	
	public String getReceiverPhone() {
		return receiverPhone;
	}
	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}
	public String getThreadId() {
		return threadId;
	}
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	public String getDts() {
		return dts;
	}
	public void setDts(String dts) {
		this.dts = dts;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getReceiverUserName() {
		return receiverUserName;
	}

	public void setReceiverUserName(String receiverUserName) {
		this.receiverUserName = receiverUserName;
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}

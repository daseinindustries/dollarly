package ly.dollar.tx.entity;

import javax.ws.rs.FormParam;

import org.apache.commons.lang.builder.ToStringBuilder;

public class SmsPostRequest {
	
	@FormParam("id")
	private String id;
	
	@FormParam("tid")
	private String tid;
	
	@FormParam("senderPhone")
	private String senderPhone;
	
	@FormParam("dollarlyPhone")
	private String dollarlyPhone;
	
	@FormParam("receiverPhone")
	private String receiverPhone;
	
	@FormParam("dts")
	private String dts;
	
	@FormParam("message")
	private String message;
	
	@FormParam("receiverUserName")
	private String receiverUserName;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public String getSenderPhone() {
		return senderPhone;
	}
	public void setSenderPhone(String sender) {
		this.senderPhone = sender;
	}
	public String getDollarlyPhone() {
		return dollarlyPhone;
	}
	public void setDollarlyPhone(String phone1) {
		this.dollarlyPhone = phone1;
	}
	public String getReceiverPhone() {
		return receiverPhone;
	}
	public void setReceiverPhone(String phone2) {
		this.receiverPhone = phone2;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDts() {
		return dts;
	}
	public void setDts(String dts) {
		this.dts = dts;
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


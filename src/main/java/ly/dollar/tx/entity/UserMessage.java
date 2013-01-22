package ly.dollar.tx.entity;


import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@SuppressWarnings("deprecation") @JsonWriteNullProperties(value=false)
public class UserMessage {

	public enum Category
	{
		NEWPHONE_PAYER, NEWPHONE_PAYEE, AUTHED_PAYER, AUTHED_PAYEE, FS_PARTIAL_PAYER, FS_PARTIAL_PAYEE 
	}
	
	@Id
	private String id;
	private String type;
	private String messageBody;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMessageBody() {
		return messageBody;
	}
	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}
	
}

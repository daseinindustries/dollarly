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
public class Reminder implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public enum Status
	{
		OPEN, PROCESSING, ONGOING, COMPLETE, FAILED, STOPPED
	}
	
	public enum Entity
	{
		Confirmation, IouOrder, PhoneConfirmation, User, Payment
	}

	@Id
	private String id;
	private Status status;
	private Entity entity;
	private String entityId;
	private String messageId;
	private Date statusLastModifiedOn = new Date();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public Entity getEntity() {
		return entity;
	}
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	public String getEntityId() {
		return entityId;
	}
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public Date getStatusLastModifiedOn() {
		return statusLastModifiedOn;
	}
	public void setStatusLastModifiedOn(Date statusLastModifiedOn) {
		this.statusLastModifiedOn = statusLastModifiedOn;
	}
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}
}

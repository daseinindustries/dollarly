package ly.dollar.tx.entity;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class DollarlyAnonPhoneResponse {
	private String status;
	private String anonPhoneId;
	private int code;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getAnonPhoneId() {
		return anonPhoneId;
	}
	public void setAnonPhoneId(String anonPhoneId) {
		this.anonPhoneId = anonPhoneId;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}
}

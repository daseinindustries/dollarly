package ly.dollar.tx.entity;

import java.io.Serializable;
import java.util.ArrayList;

import ly.dollar.tx.entity.UserPlatform.UserComPlatform;
import ly.dollar.tx.entity.UserPlatform.UserComPlatforms;
import ly.dollar.tx.entity.UserPlatform.UserPayPlatform;
import ly.dollar.tx.entity.UserPlatform.UserPayPlatforms;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static class Users extends ArrayList<User>
	{
		private static final long serialVersionUID = 1L;
	}

	private String id;
	private Long phone;
	private String email;
	private String regStatus;
	private UserPayPlatforms payPlatforms;
	private UserComPlatforms comPlatforms;

	public String getId()
	{
		return id;
	}

	public void setId(String userId)
	{
		this.id = userId;
	}

	public Long getPhone()
	{
		return phone;
	}

	public void setPhone(Long phone)
	{
		this.phone = phone;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getRegStatus() {
		return regStatus;
	}

	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}

	public UserPayPlatforms getPayPlatforms()
	{
		return payPlatforms;
	}

	public void setPayPlatforms(UserPayPlatforms payPlatforms)
	{
		this.payPlatforms = payPlatforms;
	}
	
	public UserPayPlatform getPayPlatform(ExtSystem system)
	{
		for (UserPayPlatform p : this.payPlatforms)
		{
			if (p.getSystem() == system) return p;
		}
		return null;
	}

	public UserComPlatforms getComPlatforms()
	{
		return comPlatforms;
	}

	public void setComPlatforms(UserComPlatforms comPlatforms)
	{
		this.comPlatforms = comPlatforms;
	}

	public UserComPlatform getComPlatform(ExtSystem system)
	{
		for (UserComPlatform p : this.comPlatforms)
		{
			if (p.getSystem() == system) return p;
		}
		return null;
	}
	
	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}

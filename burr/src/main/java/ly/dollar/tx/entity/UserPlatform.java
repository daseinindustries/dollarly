package ly.dollar.tx.entity;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

public abstract class UserPlatform 
{
	private String id;
	private ExtSystem system;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public ExtSystem getSystem()
	{
		return system;
	}

	public void setSystem(String system)
	{
		this.system = ExtSystem.valueOf(system.toUpperCase());
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

	// Implementations

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class UserPayPlatform extends UserPlatform implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String name;
		private String type;
		private String token;
		private String pin;

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public String getToken()
		{
			return token;
		}

		public void setToken(String token)
		{
			this.token = token;
		}

		public String getPin()
		{
			return pin;
		}

		public void setPin(String pin)
		{
			this.pin = pin;
		}
	}

	public static class UserPayPlatforms extends ArrayList<UserPayPlatform>
	{
		private static final long serialVersionUID = 1L;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class UserComPlatform extends UserPlatform implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String userName;
		private String token;
		private String tokenSecret;

		public String getUserName()
		{
			return userName;
		}

		public void setUserName(String userName)
		{
			this.userName = userName;
		}

		public String getToken()
		{
			return token;
		}

		public void setToken(String token)
		{
			this.token = token;
		}

		public String getTokenSecret()
		{
			return tokenSecret;
		}

		public void setTokenSecret(String tokenSecret)
		{
			this.tokenSecret = tokenSecret;
		}
	}

	public static class UserComPlatforms extends ArrayList<UserComPlatform>
	{
		private static final long serialVersionUID = 1L;
	}

}

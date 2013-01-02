package ly.dollar.config;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "config")
public class ConfigKeyValue<T extends Serializable>
{
	private String key;
	private T value;

	public ConfigKeyValue() {}
			
	public ConfigKeyValue(String key, T value)
	{
		this.key = key;
		this.value = value;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public T getValue()
	{
		return value;
	}

	public void setValue(T value)
	{
		this.value = value;
	}

}

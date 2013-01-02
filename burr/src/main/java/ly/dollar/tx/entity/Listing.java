package ly.dollar.tx.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

// TODO - Think about different types of money transfers

@JsonIgnoreProperties(ignoreUnknown = true)
public class Listing implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static class Listings extends ArrayList<Listing>
	{
		private static final long serialVersionUID = 1L;
	}
	
	private String id;
	private String hashtag;
	private BigDecimal price;
	private Long initialQuantity;
	private String userId;
	private Long tweetId;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getHashtag()
	{
		return hashtag;
	}

	public void setHashtag(String hashtag)
	{
		this.hashtag = hashtag;
	}

	public BigDecimal getPrice()
	{
		return price;
	}

	public void setPrice(BigDecimal price)
	{
		this.price = price;
	}

	public Long getInitialQuantity()
	{
		return initialQuantity;
	}

	public void setInitialQuantity(Long initialQuantity)
	{
		this.initialQuantity = initialQuantity;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public Long getTweetId()
	{
		return tweetId;
	}

	public void setTweetId(Long tweetId)
	{
		this.tweetId = tweetId;
	}

	@Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
	
}


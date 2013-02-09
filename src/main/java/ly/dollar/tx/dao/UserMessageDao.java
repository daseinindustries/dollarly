package ly.dollar.tx.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import ly.dollar.tx.entity.Sms;
import ly.dollar.tx.entity.UserMessage;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

public class UserMessageDao {

private final MongoOperations mongo;
	
	public UserMessageDao(MongoOperations mongo)
	{
		this.mongo = mongo;
		if (!mongo.collectionExists(UserMessage.class))
		{
			mongo.createCollection(UserMessage.class);
		}
		//ensureIndexes();
	}
	
	public void create(UserMessage p)
	{
		mongo.insert(p);
	}
	

	public void update(UserMessage p)
	{
		mongo.save(p);
	}
	
	public UserMessage findById(String id)
	{
		return mongo.findOne ( 
					new Query(where("_id").is(id)),
			    	UserMessage.class
			   );
	}
	
	public UserMessage findByType(String type)
	{
		return mongo.findOne ( 
					new Query(where("type").is(type)),
			    	UserMessage.class
			   );
	}
	
	
	
}

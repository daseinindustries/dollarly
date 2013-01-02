package ly.dollar.config;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.io.Serializable;

import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class ConfigSvcMongoImpl implements ConfigSvc
{
	//private static final Logger logger = LoggerFactory.getLogger(ConfigSvcMongoImpl.class);
	
	private final MongoOperations mongo;

	public ConfigSvcMongoImpl(MongoOperations mongo)
	{
		this.mongo = mongo;
		if (!mongo.collectionExists(ConfigKeyValue.class))
		{
			mongo.createCollection(ConfigKeyValue.class);
			init();
		}
		ensureIndexes();
	}

	@Override
	public <T extends Serializable> void set(String key, T value) 
	{
		Query q = new Query(where("key").is(key));
		Update u = new Update();
		u.set("value", value);
		mongo.upsert(q, u, ConfigKeyValue.class);
	}
	
	@Override
	public <T extends Serializable> T get(String key) 
	{
		@SuppressWarnings("unchecked")
		ConfigKeyValue<T> c = mongo.findOne ( 
				new Query(where("key").is(key)),
				ConfigKeyValue.class
		      );
		
		return c != null ? c.getValue() : null;
	}

	@Override
	public boolean acquireLock(String lockName)
	{
		// if old value was 0, lock acquired
		return mongo.findAndModify(
				new Query(where("key").is(lockName)), 
				new Update().set("value", 1), 
				ConfigKeyValue.class
			   ).getValue().equals(0); 
	}

	@Override
	public void releaseLock(String lockName)
	{
		mongo.findAndModify (
				new Query(where("key").is(lockName)), 
				new Update().set("value", 0), 
				ConfigKeyValue.class
	          );
	}
	
	private void init()
	{
		ConfigKeyValue<Integer> twitterApiLock 
			= new ConfigKeyValue<Integer>("twitter.apiLock", 0);
		this.mongo.insert(twitterApiLock);
		/*
		ConfigKeyValue<Integer> confirmExpiry  
		    = new ConfigKeyValue<Integer>("confirm.expiry", 15);
		this.mongo.insert(confirmExpiry);
		 */
	}
	
	private void ensureIndexes()
	{
		IndexOperations ops = mongo.indexOps(ConfigKeyValue.class);
		Index index = new Index().on("key", Order.ASCENDING).unique();
		ops.ensureIndex(index);
	}
}

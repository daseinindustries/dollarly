package ly.dollar.tx.dao;


import static org.springframework.data.mongodb.core.query.Criteria.where;

import ly.dollar.tx.entity.LedgerTotals;

import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;

public class UserTotalsDao {
	
	
	private final MongoOperations mongo;

	public UserTotalsDao(MongoOperations mongo)
	{
		this.mongo = mongo;
		if (!mongo.collectionExists(LedgerTotals.class))
		{
			mongo.createCollection(LedgerTotals.class);
		}
		ensureIndexes();
	}
	
	public void create(LedgerTotals t)
	{
		mongo.insert(t);
	}
	

	public void update(LedgerTotals t)
	{
		mongo.save(t);
	}
	
	public LedgerTotals findById(String id)
	{
		return mongo.findOne ( 
					new Query(where("_id").is(id)),
			    	LedgerTotals.class
			   );
	}
	
	public LedgerTotals findByPhone(Long phone)
	{
		return mongo.findOne ( 
					new Query(where("phone").is(phone)),
			    	LedgerTotals.class
			   );
	}
	
	public LedgerTotals findByUserId(String userId)
	{
		return mongo.findOne ( 
					new Query(where("userId").is(userId)),
			    	LedgerTotals.class
			   );
	}
	
	private void ensureIndexes(){
		IndexOperations ops = mongo.indexOps(LedgerTotals.class);
		Index byPhoneIndex = new Index().on("phone", Order.ASCENDING);
		ops.ensureIndex(byPhoneIndex);
		
		Index byUserIdIndex = new Index().on("userId", Order.ASCENDING);
		ops.ensureIndex(byUserIdIndex);

	}
}

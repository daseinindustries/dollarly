package ly.dollar.tx.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.Date;
import ly.dollar.tx.entity.PhoneConfirmation;
import ly.dollar.tx.entity.PhoneConfirmation.Status;

import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class PhoneConfirmationDao {
	private final MongoOperations mongo;

	public PhoneConfirmationDao(MongoOperations mongo)
	{
		this.mongo = mongo;
		if (!mongo.collectionExists(PhoneConfirmation.class))
		{
			mongo.createCollection(PhoneConfirmation.class);
		}
		ensureIndexes();
	}

	
	public void create(PhoneConfirmation c)
	{
		mongo.insert(c); 
	}

	
	public void update(PhoneConfirmation c)
	{
		mongo.save(c);
	}
	
	public PhoneConfirmation findByUserId(String userId)
	{
		return mongo.findOne(
				new Query(where("userId").is(userId)),
				PhoneConfirmation.class);
	}
	public PhoneConfirmation findByCodePhone(Integer code, Long phone)
	{
		return mongo.findOne(
				new Query(where("code").is(code)
						 .and("phone").is(phone)),
				PhoneConfirmation.class);
	}
	
	
	public PhoneConfirmation findAndConfirmByCodePhone(Integer code, Long phone)
	{
		Query q = new Query(where("code").is(code)
				 .and("phone").is(phone)
				 .and("status").nin(Status.REPLIED, 
								    Status.UNDELIVERABLE,
							        Status.EXPIRED));
		
		Update u = new Update();
		u.set("status", Status.REPLIED);
		u.set("statusLastModifiedOn", new Date());
		
		PhoneConfirmation c = mongo.findAndModify(q, u, PhoneConfirmation.class);
		if(c != null) c.setStatus(Status.REPLIED);
		
		return c;
	}
	
	public void findAndExpireOlderThan(Date date) 
	{
		Query q = new Query(where("status").nin(Status.REPLIED, 
								    Status.UNDELIVERABLE,
							        Status.EXPIRED)
				 .and("statusLastModifiedOn").lt(date));
		
		Update u = new Update();
		u.set("status", Status.EXPIRED);
		u.set("statusLastModifiedOn", new Date());
		
		mongo.findAndModify(q, u, PhoneConfirmation.class);
	}
	
	private void ensureIndexes()
	{
		IndexOperations ops = mongo.indexOps(PhoneConfirmation.class);
		Index userIdIndex = new Index().on("userId", Order.ASCENDING);
		ops.ensureIndex(userIdIndex);
		Index toCodeindex = new Index().on("phone", Order.ASCENDING)
				                       .on("code", Order.ASCENDING);
		ops.ensureIndex(toCodeindex);
		Index statusIndex = new Index().on("status", Order.ASCENDING)
                                       .on("statusLastModifiedOn", Order.ASCENDING);
        ops.ensureIndex(statusIndex);
	}
}

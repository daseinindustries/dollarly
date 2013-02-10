package ly.dollar.tx.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.Date;

import ly.dollar.tx.entity.Confirmation;
import ly.dollar.tx.entity.Confirmation.Status;

import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class ConfirmationDao {
	private final MongoOperations mongo;

	public ConfirmationDao(MongoOperations mongo)
	{
		this.mongo = mongo;
		if (!mongo.collectionExists(Confirmation.class))
		{
			mongo.createCollection(Confirmation.class);
		}
		ensureIndexes();
	}

	
	public void create(Confirmation c)
	{
		mongo.insert(c); 
	}

	
	public void update(Confirmation c)
	{
		mongo.save(c);
	}
	
	
	public Confirmation findByPurchaseOrderId(String purchaseOrderId)
	{
		return mongo.findOne(
				new Query(where("orderId").is(purchaseOrderId)),
				Confirmation.class);
	}
	
	
	public Confirmation findByCodePhone(Integer code, Long phone)
	{
		return mongo.findOne(
				new Query(where("code").is(code)
						 .and("phone").is(phone)),
				Confirmation.class);
	}
	
	
	public Confirmation findAndConfirmByCodePhone(Integer code, Long phone)
	{
		Query q = new Query(where("code").is(code)
				 .and("phone").is(phone)
				 .and("status").nin(//Status.REPLIED, 
								    Status.UNDELIVERABLE,
							        Status.EXPIRED,
							        Status.VOID));
		
		Update u = new Update();
		u.set("status", Status.REPLIED);
		u.set("statusLastModifiedOn", new Date());
		
		Confirmation c = mongo.findAndModify(q, u, Confirmation.class);
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
		
		mongo.findAndModify(q, u, Confirmation.class);
	}
	
	private void ensureIndexes()
	{
		IndexOperations ops = mongo.indexOps(Confirmation.class);
		Index poIdIndex = new Index().on("orderId", Order.ASCENDING).unique();
		ops.ensureIndex(poIdIndex);
		Index toCodeindex = new Index().on("phone", Order.ASCENDING)
				                       .on("code", Order.ASCENDING);
		ops.ensureIndex(toCodeindex);
		Index statusIndex = new Index().on("status", Order.ASCENDING)
                                       .on("statusLastModifiedOn", Order.ASCENDING);
        ops.ensureIndex(statusIndex);
	}


	public Confirmation findById(String id) {
		return mongo.findOne ( 
				new Query(where("_id").is(id)),
		    	Confirmation.class
		   );
	}

}

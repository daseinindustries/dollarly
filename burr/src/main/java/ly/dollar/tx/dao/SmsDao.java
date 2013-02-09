package ly.dollar.tx.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



import ly.dollar.tx.entity.Sms;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class SmsDao {
	
	private final MongoOperations mongo;
	
	public SmsDao(MongoOperations mongo)
	{
		this.mongo = mongo;
		if (!mongo.collectionExists(Sms.class))
		{
			mongo.createCollection(Sms.class);
		}
		//ensureIndexes();
	}
	
	public void create(Sms p)
	{
		mongo.insert(p);
	}
	

	public void update(Sms p)
	{
		mongo.save(p);
	}
	
	public Sms findById(String id)
	{
		return mongo.findOne ( 
					new Query(where("_id").is(id)),
			    	Sms.class
			   );
	}
	
	public Sms findBySmsIdThreadId(String smsId, String tid){
		Query q = new Query(where("extSystemId").is(smsId).
				and("threadId").is("tid"));
		return mongo.findOne(q,  Sms.class);
		
	}
	
	public List<Sms> findByStatus(String status) {
		
		Query q = new Query(where("status").is(status));
		return mongo.find(q, Sms.class);	
	}

	public List<Sms> getUnprocessedToProcessing() {
		Query q = new Query();
		q.addCriteria(where("status").is("UNPROCESSED"));
		Collection<Sms> unpro = mongo.find(q, Sms.class);

		ArrayList<Sms> smss = new ArrayList<Sms>();
		for (Sms i : unpro){
			
			Sms n = updateToProcessing(i.getId());
			smss.add(n);
			System.out.println("sms updated: " +n.toString());
		}
		return smss;
	}
	
	public Sms updateToProcessing(String id){
		Query q = new Query();
		q.addCriteria(where("status").is("UNPROCESSED"));
		q.addCriteria(where("_id").is(id));

		Update u = new Update();
		u.set("status", "PROCESSING");

		Sms p = mongo.findAndModify(q, u, Sms.class);
		p.setStatus("PROCESSING");
		return p;
	}
}

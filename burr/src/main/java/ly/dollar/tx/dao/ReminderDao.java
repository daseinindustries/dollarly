package ly.dollar.tx.dao;

import java.util.ArrayList;
import java.util.List;

import ly.dollar.tx.entity.Reminder;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import org.springframework.data.mongodb.core.query.Query;

import org.springframework.data.mongodb.core.MongoOperations;

public class ReminderDao {
private final MongoOperations mongo;
	
	public ReminderDao(MongoOperations mongo)
	{
		this.mongo = mongo;
		if (!mongo.collectionExists(Reminder.class))
		{
			mongo.createCollection(Reminder.class);
		}
		//ensureIndexes();
	}
	
	public void create(Reminder p)
	{
		mongo.insert(p);
	}
	

	public void update(Reminder p)
	{
		mongo.save(p);
	}
	
	public Reminder findById(String id)
	{
		return mongo.findOne ( 
					new Query(where("_id").is(id)),
			    	Reminder.class
			   );
	}
	
	public Reminder findByEntityId(String entityId)
	{
		return mongo.findOne ( 
					new Query(where("entityId").is(entityId)),
			    	Reminder.class
			   );
	}
	
	public Reminder findByEntityIdAndMessageId(String entityId, String messageId)
	{
		return mongo.findOne ( 
				new Query(where("entityId").is(entityId).and("messageId").is(messageId)),
		    	Reminder.class
		   );
		
	}
	
	public List<Reminder> findByStatus(Reminder.Status status)
	{
		Query q = new Query();
		q.addCriteria(where("status").is(status));
		List<Reminder> rems = mongo.find(q, Reminder.class);
		return rems;
	}
	
	public List<Reminder> updateOpenToProcessing(List<Reminder> opens)
	{
		if(opens != null){
		ArrayList<Reminder> procs = new ArrayList<Reminder>();
		for(Reminder r : opens){
			r.setStatus(Reminder.Status.PROCESSING);
			procs.add(r);
			update(r);
		}
		return procs;
		}
		else return null;
	}
}

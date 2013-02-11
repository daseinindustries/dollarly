package ly.dollar.tx.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import ly.dollar.tx.entity.IouOrder;

import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class IouOrderDao {
	
	private final MongoOperations mongo;

	public IouOrderDao(MongoOperations mongo)
	{
		this.mongo = mongo;
		if (!mongo.collectionExists(IouOrder.class))
		{
			mongo.createCollection(IouOrder.class);
		}
		ensureIndexes();
	}
	
	public IouOrder createAndReturn(IouOrder i) {
		throw new RuntimeException("Method unimplemented.");
	}
	
	public void create(IouOrder p)
	{
		mongo.insert(p);
	}
	
	public void update(IouOrder p)
	{
		mongo.save(p);
	}
	
	public void updateUserFundingStatus(String userId, String status){
	
		Query q = new Query();
		q.addCriteria(where("payerUserId").is(userId));
		q.addCriteria(where("status").is(IouOrder.Status.OPEN));
		Collection<IouOrder> pays = mongo.find(q, IouOrder.class);
		  
		Update u = new Update();
		u.set("payerFundingStatus", status);
		
		for (IouOrder i : pays){
			i.setPayerFundingStatus(status);
			update(i);
			System.out.println("pays: " +i.toString());
		}
		
		Query e = new Query();
		e.addCriteria(where("payeeUserId").is(userId));
		e.addCriteria(where("status").is(IouOrder.Status.OPEN));
		Collection<IouOrder> collects = mongo.find(e, IouOrder.class);
		  
		Update ue = new Update();
		ue.set("payeeFundingStatus", status);
		for (IouOrder i : collects){
			i.setPayeeFundingStatus(status);
			update(i);
			System.out.println("collects: " +i.toString());
		}
	}
	
	public void updateUserStatus(String userId, String status, String handle) 
	{
		Query q = new Query();
		q.addCriteria(where("payerUserId").is(userId));
		q.addCriteria(where("status").is(IouOrder.Status.OPEN));
		Collection<IouOrder> pays = mongo.find(q, IouOrder.class);

		for (IouOrder i : pays){
			i.setPayerFundingStatus(status);
			i.setPayerHandle(handle);
			update(i);
			System.out.println("pays: " +i.toString());
		}
		
		Query e = new Query();
		e.addCriteria(where("payeeUserId").is(userId));
		e.addCriteria(where("status").is(IouOrder.Status.OPEN));
		Collection<IouOrder> collects = mongo.find(e, IouOrder.class);
		
		for (IouOrder i : collects){
			i.setPayeeFundingStatus(status);
			i.setPayeeHandle(handle);
			update(i);
			System.out.println("collects: " +i.toString());
		}		
	}

	//NEW
	public void updateUserByPhone(String phone, String userId, String status, String handle) {
		
		Query e = new Query();
		e.addCriteria(where("extSystemUserName").is(phone));
		e.addCriteria(where("payerFundingStatus").is("NEW_PHONE"));
		Collection<IouOrder> pays = mongo.find(e, IouOrder.class);
		for (IouOrder i : pays){
			i.setPayerFundingStatus(status);
			i.setPayerUserId(userId);
			i.setPayerHandle(handle);
			update(i);
			System.out.println("pays: " +i.toString());
		}
		
		
		Query q = new Query();
		q.addCriteria(where("mention").is(phone));
		q.addCriteria(where("payeeFundingStatus").is("NEW_PHONE"));
		Collection<IouOrder> collects = mongo.find(q, IouOrder.class);
		  
		for (IouOrder i : collects){
			i.setPayeeFundingStatus(status);
			i.setPayeeUserId(userId);
			i.setPayeeHandle(handle);
			update(i);
			System.out.println("collects: " +i.toString());
		}		
	}
	
	public IouOrder findById(String id)
	{
		return mongo.findOne ( 
					new Query(where("_id").is(id)),
			    	IouOrder.class
			   );
	}
	
	public IouOrder findByExternalSystemIdAndName(String id, String externalSystem)
	{
		return mongo.findOne ( 
					new Query(where("extSystemId").is(id).and("extSystem").is(externalSystem)),
			    	IouOrder.class
			   );
	}

	public Collection<IouOrder> findByPayeeUserId(String payeeUserId)
	{
		return mongo.find ( 
					new Query(where("payeeUserId").is(payeeUserId)),
			    	IouOrder.class
			   );
	}
	
	public void findAndRemoveById(String id)
	{
		mongo.findAndRemove ( 
					new Query(where("payeeUserId").is(id)),
			    	IouOrder.class
			   );
		mongo.findAndRemove ( 
				new Query(where("payerUserId").is(id)),
		    	IouOrder.class
		   );
	}

	public Collection<IouOrder> findByPayerUserId(String payerUserId)
	{
		return mongo.find ( 
					new Query(where("payerUserId").is(payerUserId)),
			    	IouOrder.class
			   );
	}
	

	public Collection<IouOrder> findByPayerUserIdAndCreateDate(String payerUserId, Date onOrAfter)
    {
	    return mongo.find ( 
                new Query(where("payerUserId").is(payerUserId).and("createdOn").gte(onOrAfter)),
                IouOrder.class
           );
    }
	
	public Collection<IouOrder> findByPayeeUserIdAndCreateDate(String payeeUserId, Date onOrAfter)
    {
	    return mongo.find ( 
                new Query(where("payeeUserId").is(payeeUserId).and("createdOn").gte(onOrAfter)),
                IouOrder.class
           );
    }
	
	public List<IouOrder> findOpenIousByUserId(String userId, String payParty){
		if (payParty.equals("payee"))
		{
			return mongo.find(
					new Query(where("payeeUserId").is(userId).and("status").is(IouOrder.Status.OPEN)),
					IouOrder.class);
		}
		else if (payParty.equals("payer")){
			return mongo.find(
					new Query(where("payerUserId").is(userId).and("status").is(IouOrder.Status.OPEN)),
					IouOrder.class);

		} else {
			return null;
		}
	}
	
	public List<IouOrder> findOpenIousByPhone(String phone, String payParty){
	
		if (payParty.equals("payee"))
		{
			return mongo.find(
					new Query(where("mention").is(phone).and("status").is(IouOrder.Status.OPEN)),
					IouOrder.class);
		}
		else if (payParty.equals("payer")){
			return mongo.find(
					new Query(where("extSystemUserName").is(phone).and("status").is(IouOrder.Status.OPEN)),
					IouOrder.class);

		} else {
			return null;
		}
		
	}
	
	
	public IouOrder updateStatusToConfirm(String id) {
		Query q = new Query();
		q.addCriteria(where("_id").is(id));
		q.addCriteria(where("status").is(IouOrder.Status.OPEN));
		
		Update u = new Update();
		u.set("status", IouOrder.Status.PENDING_CONFIRMATION);
		
		IouOrder i = mongo.findAndModify(q, u, IouOrder.class);
		i.setStatus(IouOrder.Status.PENDING_CONFIRMATION);
		return i;	
	}
	
	public IouOrder updateStatusToVoid(String id) {
		Query q = new Query();
		q.addCriteria(where("_id").is(id));
		q.addCriteria(where("status").in(IouOrder.Status.OPEN, IouOrder.Status.PENDING_CONFIRMATION));
		
		Update u = new Update();
		u.set("status", IouOrder.Status.VOID);
		IouOrder i = mongo.findAndModify(q, u, IouOrder.class);
		i.setStatus(IouOrder.Status.VOID);
		return i;
		
	}
	private void ensureIndexes()
	{
		IndexOperations ops = mongo.indexOps(IouOrder.class);
		
		Index payeeUserIndex = new Index().on("payeeUserId", Order.ASCENDING);
		ops.ensureIndex(payeeUserIndex);
		
		Index payerUserIndex = new Index().on("payerUserId", Order.ASCENDING);
		ops.ensureIndex(payerUserIndex);
		
		Index unique = new Index()
		.on("extSystem", Order.ASCENDING)
		.on("extSystemId", Order.ASCENDING).unique();
	ops.ensureIndex(unique);
		
	}




	


}

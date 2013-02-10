package ly.dollar.tx.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.List;

import ly.dollar.tx.entity.Payment;
import ly.dollar.tx.entity.Payment.Status;

import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class PaymentDaoImpl implements PaymentDao
{
	//private static final Logger logger = LoggerFactory.getLogger(PaymentDaoImpl.class);
	
	private final MongoOperations mongo;
	
	public PaymentDaoImpl(MongoOperations mongo)
	{
		this.mongo = mongo;
		if (!mongo.collectionExists(Payment.class))
		{
			mongo.createCollection(Payment.class);
		}
		ensureIndexes();
	}
	@Override
	public void create(Payment p)
	{
		mongo.insert(p); 
	}

	@Override
	public void update(Payment p)
	{
		mongo.save(p); 
	}
	
	@Override
	public Payment findByPurchaseOrderId(String purchaseOrderId)
	{
		return mongo.findOne ( 
				new Query(where("purchaseOrderId").is(purchaseOrderId)),
				Payment.class
		   );
	}
	
	@Override
	public List<Payment> findByStatus(Status status)
	{
		Query q = new Query(where("status").is(Status.UNPROCESSED));
		return mongo.find(q, Payment.class);
	}

	@Override
	public Payment findAndSetStatusToProcessing(String id)
	{
		Query q = new Query();
		q.addCriteria(where("status").is(Status.UNPROCESSED));
		q.addCriteria(where("_id").is(id));

		Update u = new Update();
		u.set("status", Status.PROCESSING);

		Payment p = mongo.findAndModify(q, u, Payment.class);
		p.setStatus(Status.PROCESSING);
		return p;
	}
	
	//payment index
	private void ensureIndexes()
	{
		IndexOperations ops = mongo.indexOps(Payment.class);
		Index purchaseOrderIndex = new Index().on("purchaseOrderId", Order.ASCENDING);
		ops.ensureIndex(purchaseOrderIndex);
		Index statusIndex = new Index().on("status", Order.ASCENDING);
		ops.ensureIndex(statusIndex);
	}
	
}


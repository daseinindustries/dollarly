package ly.dollar.tx.dao;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import ly.dollar.tx.entity.Inventory;

import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class InventoryDaoImpl implements InventoryDao
{
    //private static final Logger logger = LoggerFactory.getLogger(InventoryDaoImpl.class);
	
	private final MongoOperations mongo;

	public InventoryDaoImpl(MongoOperations mongo)
	{
		this.mongo = mongo;
		if (!mongo.collectionExists(Inventory.class))
		{
			mongo.createCollection(Inventory.class);
		}
		ensureIndexes();
	}

	@Override
	public void create(String listingId, long initialQuantity)
	{
		Inventory inv = new Inventory(listingId, initialQuantity);
		mongo.insert(inv); 
	}
	
	@Override
	public void update(String listingId, long newQuantity)
	{
		mongo.findAndModify(
				new Query(where("listingId").is(listingId)), 
				new Update().set("quantity", newQuantity), 
				Inventory.class);
	}
	
	@Override
	public Inventory findByListingId(String listingId)
	{
		return mongo.findOne ( 
				new Query(where("listingId").is(listingId)),
				Inventory.class
		      );
	}

	@Override
	public long decrement(String listingId, long quantity)
	{
		// TODO - Handle partial fulfillment
		Inventory i = mongo.findAndModify(
				new Query(where("listingId").is(listingId).and("quantity").gte(quantity)), 
				new Update().inc("quantity", -quantity), 
				Inventory.class);
		
		return i == null ? 0 : quantity;
	}

	@Override
	public void increment(String listingId, long quantity)
	{
		mongo.findAndModify(
				new Query(where("listingId").is(listingId).and("quantity").gte(quantity)), 
				new Update().inc("quantity", quantity), 
				Inventory.class);
	}

	@Override
	public boolean isAvailable(String listingId)
	{
		return isAvailable(listingId, 1);
	}

	@Override
	public boolean isAvailable(String listingId, long quantity) 
	{
		Inventory inv = mongo.findOne ( 
					new Query(where("listingId").is(listingId)),
					Inventory.class
			      );

		if (inv == null)
		{
			System.out.println("Inventory record missing for " + listingId);
		}

		return inv != null && inv.getQuantity() >= quantity;
	}
	
	private void ensureIndexes()
	{
		IndexOperations ops = mongo.indexOps(Inventory.class);
		Index index = new Index().on("listingId", Order.ASCENDING).unique();
		ops.ensureIndex(index);
	}



}
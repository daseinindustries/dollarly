package ly.dollar.tx.svc;

import ly.dollar.tx.entity.Inventory;

public interface InventorySvc
{
	void create(String listingId, long initialQuantity);
	
	void update(String listingId, long newQuantity);

	void add(String listingId, long quantity); // Atomic
	
	long remove(String listingId, long quantity); // Atomic

	boolean available(String listingId, long quantity);
	
	Inventory getByListingId(String listingId);
}
package ly.dollar.tx.dao;

import ly.dollar.tx.entity.Inventory;

// TODO - Collapse increment and decrement methods
public interface InventoryDao
{
	void create(String listingId, long initialQuantity);
	
	void update(String listingId, long newQuantity);
	
	Inventory findByListingId(String listingId);

	/**
	 * Atomically reduces available quantity in inventory by quantity parameter. 
	 * @return The number removed from inventory. It is 
	 * 	       possible that this could be less than the quantity 
	 * 	       parameter. ex. Buyer requests two, only one left available.
	 */
	long decrement(String listingId, long quantity);
	
	/**
	 * Atomically increases available quantity in inventory by quantity parameter. 
	 */
	void increment(String listingId, long quantity);
	
	boolean isAvailable(String listingId);
	
	boolean isAvailable(String listingId, long quantity);
}

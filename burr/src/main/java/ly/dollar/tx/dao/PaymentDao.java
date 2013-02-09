package ly.dollar.tx.dao;
import java.util.List;

import ly.dollar.tx.entity.Payment;
import ly.dollar.tx.entity.Payment.Status;

public interface PaymentDao
{
	void create(Payment p);

	void update(Payment p);
	
	Payment findByPurchaseOrderId(String purchaseOrderId);

	List<Payment> findByStatus(Status status);
	
	/**
	 * Atomically finds an UNPROCESSED payment document
	 * by id and updates its status to PROCESSING
	 */
	Payment findAndSetStatusToProcessing(String id);
}

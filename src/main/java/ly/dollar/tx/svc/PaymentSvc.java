package ly.dollar.tx.svc;

import java.util.List;

import ly.dollar.tx.entity.Payment;
import ly.dollar.tx.entity.User;

public interface PaymentSvc
{
	void create(Payment p);

	void update(Payment p);

	void execute(Payment p, User payer, User payee);

	/*
	 * Retrieves the payment and changes its status to PROCESSING.
	 */
	Payment lockForProcessing(String id); // Atomic

	Payment getByPurchaseOrderId(String purchaseOrderId);

	List<Payment> getAllUnprocessed();
}
package ly.dollar.tx.svc;

import java.math.BigDecimal;
import java.util.List;

import ly.dollar.tx.entity.Payment;
import ly.dollar.tx.entity.User;

public interface PaymentSvc
{
    static final BigDecimal SPENDING_LIMIT_AMOUNT = new BigDecimal("250");
    static final Integer SPENDING_LIMIT_WINDOW_DAYS = 7;

	void create(Payment p);

	void update(Payment p);

	void execute(Payment p, User payer, User payee);
	
	void updateTotals(Payment p);

	/*
	 * Retrieves the payment and changes its status to PROCESSING.
	 */
	Payment lockForProcessing(String id); // Atomic

	Payment getByPurchaseOrderId(String purchaseOrderId);

	List<Payment> getAllUnprocessed();
	
	boolean isWithinSpendingLimits(Payment payment);
	
}
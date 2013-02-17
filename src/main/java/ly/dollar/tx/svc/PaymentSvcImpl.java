package ly.dollar.tx.svc;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ly.dollar.tx.dao.PaymentDao;
import ly.dollar.tx.dao.UserTotalsDao;
import ly.dollar.tx.entity.LedgerTotals;
import ly.dollar.tx.entity.Payment;
import ly.dollar.tx.entity.Payment.Status;
import ly.dollar.tx.entity.User;

public abstract class PaymentSvcImpl implements PaymentSvc
{
    protected final PaymentDao paymentDao;
    protected final TransformSvc transformSvc;
    protected final UserTotalsDao userTotalsDao;

    public PaymentSvcImpl(PaymentDao paymentDao, TransformSvc transformSvc,
    		UserTotalsDao userTotalsDao)
    {
        this.transformSvc = transformSvc;
        this.paymentDao = paymentDao;
        this.userTotalsDao = userTotalsDao;
    }
    
    @Override
    public abstract void execute(Payment p, User payer, User payee);
    
    @Override
    public void create(Payment p)
    {
        paymentDao.create(p);
    }

    @Override
    public void update(Payment p)
    {
        paymentDao.update(p);
    }
    
    @Override
    public Payment getByPurchaseOrderId(String purchaseOrderId)
    {
        return paymentDao.findByPurchaseOrderId(purchaseOrderId);
    }

    @Override
    public List<Payment> getAllUnprocessed()
    {
        return paymentDao.findByStatus(Status.UNPROCESSED);
    }
    
    @Override
    public Payment lockForProcessing(String id)
    {
        return paymentDao.findAndSetStatusToProcessing(id);
    }
    
    @Override
    public void updateTotals(Payment p)
    {
        BigDecimal am = p.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP);

        LedgerTotals payeeTotals = userTotalsDao.findByUserId(p.getPayeeUserId());
        if (payeeTotals == null)
        {
            payeeTotals = new LedgerTotals();
        }
        BigDecimal collected = payeeTotals.getPastCollects().setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal collUpdate = collected.add(am);
        payeeTotals.setPastCollects(collUpdate);
        userTotalsDao.update(payeeTotals);

        LedgerTotals payerTotals = userTotalsDao.findByUserId(p.getPayerUserId());
        if (payerTotals == null)
        {
            payerTotals = new LedgerTotals();
        }
        BigDecimal payed = payerTotals.getPastPays().setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal payUpdate = payed.add(am);
        payerTotals.setPastPays(payUpdate);
        userTotalsDao.update(payerTotals);
    }

    @Override
    public boolean isWithinSpendingLimits(Payment payment)
    {
        return true;
    }
    
    @Override
    public boolean isLessThanMaxAllowable(Payment payment)
    {
        return true;
    }

    protected Date windowStartDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -SPENDING_LIMIT_WINDOW_DAYS);
        return cal.getTime();
    }

}

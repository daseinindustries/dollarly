package ly.dollar.tx.svc;

import java.util.List;

import ly.dollar.tx.dao.PaymentDao;
import ly.dollar.tx.entity.Payment;
import ly.dollar.tx.entity.Payment.Status;
import ly.dollar.tx.entity.User;

public abstract class PaymentSvcImpl implements PaymentSvc
{

    protected final PaymentDao paymentDao;
    protected final TransformSvc transformSvc;

    public PaymentSvcImpl(PaymentDao paymentDao, TransformSvc transformSvc)
    {
        this.transformSvc = transformSvc;
        this.paymentDao = paymentDao;
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

}

package ly.dollar.tx.svc;

import java.math.BigDecimal;
import java.util.List;

import ly.dollar.integrations.dwolla.DwollaIntegrationAPI;
import ly.dollar.integrations.dwolla.DwollaSendMoneyInfo;
import ly.dollar.integrations.dwolla.DwollaTransactionResponse;
import ly.dollar.tx.dao.PaymentDao;
import ly.dollar.tx.entity.ExtSystem;
import ly.dollar.tx.entity.Payment;
import ly.dollar.tx.entity.Payment.Status;
import ly.dollar.tx.entity.User;
import ly.dollar.tx.entity.UserPlatform.UserPayPlatform;

public class PaymentSvcDwollaImpl implements PaymentSvc
{
	//private static final Logger logger = LoggerFactory.getLogger(PaymentSvcDwollaImpl.class);
	
	private final PaymentDao paymentDao;
	private final DwollaIntegrationAPI dwollaClient;
	private final TransformSvc transformSvc;


	
	
	public PaymentSvcDwollaImpl(PaymentDao paymentDao, TransformSvc transformSvc)
	{
		this.transformSvc = transformSvc;
		this.paymentDao = paymentDao;
		// TODO - Move this into spring config
		this.dwollaClient = new DwollaIntegrationAPI();
	}

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
	// TODO - There should be a payment result class
	// TODO - Think about what happens when auth fails for a user
	public void execute(Payment payment, User payer, User payee)
	{
		UserPayPlatform payerDwollaInfo = payer.getPayPlatform(ExtSystem.DWOLLA);
		UserPayPlatform payeeDwollaInfo = payee.getPayPlatform(ExtSystem.DWOLLA);
		
		try
		{
			DwollaSendMoneyInfo info = new DwollaSendMoneyInfo();
			info.setToken(payerDwollaInfo.getToken());
			info.setPin(transformSvc.transformMessage(payerDwollaInfo.getPin()));
			info.setAmount(payment.getAmount().toPlainString());
			info.setType(payerDwollaInfo.getType());
			info.setDestinationID(payeeDwollaInfo.getId());
			info.setNotes(payment.getId());
			BigDecimal am = payment.getAmount().setScale(2, BigDecimal.ROUND_DOWN);
			float fam = am.floatValue();
			
			if(fam <= 4.99 && fam >= 1.00){
				info.setFacilitatorAmount("0.25");
			} else if (fam <= 0.99 && fam >= 0.50){
				info.setFacilitatorAmount("0.10");
			} else if ( fam < 0.50 ){
				info.setFacilitatorAmount("0");
			}
			
			
			DwollaTransactionResponse result 
				= dwollaClient.sendMoneyWithPIN(info);

			if (result.getSuccess())
			{
				payment.setStatus(Status.PROCESSED);
				payment.setExtSystemId(String.valueOf(result.getBody()));
				payment.succeed(result.getMessage());
			}
			else
			{
				payment.setStatus(Status.FAILED);
				payment.fail(result.getMessage());
				//
			}

			payment.setExtSystem(ExtSystem.DWOLLA);
		}
		catch (Exception e)
		{
			payment.setStatus(Status.FAILED);
			payment.fail(e.getMessage());
		}
		
		System.out.println("payment="+payment);
		paymentDao.update(payment);
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

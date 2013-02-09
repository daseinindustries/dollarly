package ly.dollar.tx.svc;

import java.math.BigDecimal;

import ly.dollar.integrations.dwolla.DwollaIntegrationAPI;
import ly.dollar.integrations.dwolla.DwollaSendMoneyInfo;
import ly.dollar.integrations.dwolla.DwollaTransactionResponse;
import ly.dollar.tx.dao.PaymentDao;
import ly.dollar.tx.dao.UserTotalsDao;
import ly.dollar.tx.entity.ExtSystem;
import ly.dollar.tx.entity.Payment;
import ly.dollar.tx.entity.Payment.Status;
import ly.dollar.tx.entity.User;
import ly.dollar.tx.entity.UserPlatform.UserPayPlatform;

public class PaymentSvcDwollaImpl extends PaymentSvcImpl
{
	
	private final DwollaIntegrationAPI dwollaClient;

	public PaymentSvcDwollaImpl(PaymentDao paymentDao, TransformSvc transformSvc, UserTotalsDao userTotalsDao)
	{
	    super(paymentDao, transformSvc, userTotalsDao);
		this.dwollaClient = new DwollaIntegrationAPI();
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
               // this.updateTotals(payment);

			}
			else
			{
				payment.setStatus(Status.FAILED);
				payment.fail(result.getMessage());
				//
			}
		}
		catch (Exception e)
		{
			payment.setStatus(Status.FAILED);
			payment.fail(e.getMessage());
		}
		
		payment.setExtSystem(ExtSystem.DWOLLA);
		System.out.println("payment="+payment);
		paymentDao.update(payment);
	}

}

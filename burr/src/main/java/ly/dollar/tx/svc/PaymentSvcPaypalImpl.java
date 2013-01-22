package ly.dollar.tx.svc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ly.dollar.tx.dao.PaymentDao;
import ly.dollar.tx.entity.ExtSystem;
import ly.dollar.tx.entity.Payment;
import ly.dollar.tx.entity.User;
import ly.dollar.tx.entity.Payment.Status;
import ly.dollar.tx.entity.UserPlatform.UserPayPlatform;

import com.paypal.svcs.services.AdaptivePaymentsService;
import com.paypal.svcs.types.ap.PayError;
import com.paypal.svcs.types.ap.PayRequest;
import com.paypal.svcs.types.ap.PayResponse;
import com.paypal.svcs.types.ap.Receiver;
import com.paypal.svcs.types.ap.ReceiverList;
import com.paypal.svcs.types.common.ErrorData;
import com.paypal.svcs.types.common.RequestEnvelope;

public class PaymentSvcPaypalImpl extends PaymentSvcImpl
{

    private AdaptivePaymentsService paypalClient;
    
    public PaymentSvcPaypalImpl(PaymentDao paymentDao, TransformSvc transformSvc, String propsFile) throws IOException
    {
        super(paymentDao, transformSvc);
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(propsFile);
        paypalClient = new AdaptivePaymentsService(in);
    }

    @Override
	public void execute(Payment payment, User payer, User payee)
    {
        UserPayPlatform payerInfo = payer.getPayPlatform(ExtSystem.PAYPAL);
        UserPayPlatform payeeInfo = payee.getPayPlatform(ExtSystem.PAYPAL);
        
        PayRequest payRequest = new PayRequest();
        payRequest.setActionType("PAY");
        payRequest.setFeesPayer("SENDER");
        payRequest.setCurrencyCode("USD");
        payRequest.setCancelUrl("http://beta.dollar.ly?cancel"); // required but not relevant
        payRequest.setReturnUrl("http://beta.dollar.ly?return"); // required but not relevant
        payRequest.setRequestEnvelope(new RequestEnvelope("en_US"));
        payRequest.setTrackingId(payment.getId());
        payRequest.setReceiverList(makeReceiver(payeeInfo, payment));
        payRequest.setSenderEmail(payerInfo.getEmail());
        payRequest.setPreapprovalKey(payerInfo.getPreapprovalKey());
        
        try
	    {
		PayResponse payResponse = paypalClient.pay(payRequest);
		if (failed(payResponse))
		    {
			printErrors(payment, payRequest, payResponse);
			throw new Exception("Paypal transaction failed. See logs for details.");
		    } else
		    {
			payment.setStatus(Status.PROCESSED);
			payment.setExtSystemId(String.valueOf(payResponse.getPayKey()));
			payment.succeed(payResponse.getPaymentExecStatus());
		    }

	    } catch (Exception e)
	    {
		payment.setStatus(Status.FAILED);
		payment.fail(e.getMessage());
	    }
        
        payment.setExtSystem(ExtSystem.PAYPAL);
        System.out.println("payment="+payment.toString());
        paymentDao.update(payment);
    }
    
    private ReceiverList makeReceiver(UserPayPlatform payeeInfo, Payment payment)
    {
        Receiver receiver = new Receiver();
        receiver.setAmount(payment.getAmount().doubleValue());
        receiver.setEmail(payeeInfo.getEmail());
        receiver.setPaymentType("PERSONAL");
        List<Receiver> receivers = new ArrayList<Receiver>();
        receivers.add(receiver);

        return new ReceiverList(receivers);
    }
    
    private boolean failed(PayResponse response)
    {
        return (response.getError() != null && response.getError().size() > 0)
	    || (response.getPayErrorList() != null && response.getPayErrorList().getPayError().size() > 0);
    }
    
    private void printErrors(Payment payment, PayRequest req, PayResponse resp)
    {
        try
	    {
		System.err.println("payRequest=["+req.toNVPString()+"]");
	    } catch (UnsupportedEncodingException e1)
	    {
		// will never happen
	    }
        
        try
	    {
		System.err.println("Pay errors:");
		for (PayError pe : resp.getPayErrorList().getPayError())
		    {
			System.err.println("\t" + pe.getError().getMessage());
		    }
	    } catch (Exception e)
	    {
		System.err.println("\tNone. " + e.getMessage());
	    }
 
        try
	    {
		System.err.println("Errors:");
		for (ErrorData ed : resp.getError())
		    {
			System.err.println("\t" + ed.getMessage());
		    }
	    } catch (Exception e)
	    {
		System.err.println("\tNone. " + e.getMessage());
	    }
    }


}
package ly.dollar.tx.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

import ly.dollar.tx.entity.Confirmation;
import ly.dollar.tx.entity.IouOrder;
import ly.dollar.tx.entity.Payment;
import ly.dollar.tx.entity.PhoneConfirmation;

import ly.dollar.tx.entity.Confirmation.Status;
import ly.dollar.tx.svc.ConfirmationSvc;
import ly.dollar.tx.svc.IouOrderSvc;
import ly.dollar.tx.svc.PaymentSvc;
import ly.dollar.tx.svc.PhoneConfirmationSvc;
import ly.dollar.tx.svc.UserOnboardSvcImpl;

@Path("twilio")
public class TwilioResource {


	public static final String userBaseUrl = "https://thomas-currensea.rhcloud.com/jefferson/user/";
	public static final String phoneVerifyURL = "https://thomas-currensea.rhcloud.com/jefferson/user/confirmation/phone/";

	private IouOrderSvc iouOrderSvc;
	private ConfirmationSvc confirmationSvc;
	private PaymentSvc paymentSvc;
	private UserOnboardSvcImpl userOnboardSvc;
	private PhoneConfirmationSvc phoneConfirmationSvc;

	// This is fucking stupid take it out of here.
	@POST
	@Path("modal")
	public void postModalMessage(@FormParam("message") String message,
			@FormParam("to") String to, @FormParam("from") String from) {
		Long fromL = Long.valueOf(from.replaceAll("[^\\d]", ""));
		Long toL = Long.valueOf(to.replaceAll("[^\\d]", ""));
		userOnboardSvc.sendModalReminder(message, toL, fromL);
	}

	@POST
	@Path("confirmation")
	public void postConfirmation(@FormParam("From") String from,
			@FormParam("Body") String body) {
		Long phone = Long.valueOf(from.replaceAll("[^\\d]", ""));
		Integer code = Integer.valueOf(body.replaceAll("[^\\d]", ""));
		//check for garbage <-- 1.1 Release
		if(code != null){
			
		// if four digit, then po
		if (code.intValue() < 10000) {
			System.out.println("Searching for confirmation with phone=" + phone
					+ " and code=" + code);

			// Attempt to confirm
			Confirmation c = confirmationSvc.confirm(phone, code);
			if (c != null) {
				System.out
						.println("TWILIO: Ready to confirm and start payment process!");
				IouOrder iouOrder = iouOrderSvc.getById(c.getOrderId());
				Payment payment = new Payment(iouOrder);
				paymentSvc.create(payment);

			} else {
				System.out.println("Was not able to confirm with phone="
						+ phone + " and code=" + code);
				c = confirmationSvc.getByPhoneCode(phone, code);
				if (c == null) {
					confirmationSvc.createAndSendFailureMessage(phone, code);
					
				} else if (c.getStatus() == Status.EXPIRED) {
					IouOrder i = iouOrderSvc.getById(c.getOrderId());
					confirmationSvc.createAndSendExpireFailureMessage(phone, code, i);
				}
			}
		} else { //else five digit phone verification
			System.out.println("Phone Verification: confirmation with phone="
					+ phone + " and code=" + code);
			PhoneConfirmation pc = phoneConfirmationSvc.confirm(phone, code);
			if (pc != null) {
				System.out.println("TWILIO: phone verification confirmed!");
				pc.setStatus(PhoneConfirmation.Status.CONFIRMED);
				phoneConfirmationSvc.update(pc);
				System.out.println("UPDATING JEFF");
				try {
					this.postPhoneConfirmation(pc.getUserId(), phone);
				} catch (Exception e) {
					e.printStackTrace();
				}
				phoneConfirmationSvc.createAndSendConfirmMessage(phone);
			} else {
				System.out.println("Dollar.ly not able to verify with phone="
						+ phone + " and code=" + code);
				pc = phoneConfirmationSvc.getByPhoneCode(phone, code);
				if (pc == null) {
					phoneConfirmationSvc.createAndSendFailureMessage(phone,
							code, "PHONE_CONF_INVALID_CODE");
				} else if (pc.getStatus() == PhoneConfirmation.Status.EXPIRED) {
					phoneConfirmationSvc.createAndSendFailureMessage(phone,
							code, "PHONE_CONF_EXPIRED_CODE");
				}
			}
		}
	}
	else
	{
		System.out.println("Garbage Confirmation Message: " + body + " from: " + phone );		
	}
}

	@POST
	@Path("status/{orderId}")
	public void postStatus(@PathParam("orderId") String orderId,
			@FormParam("SmsStatus") String smsStatus) {

		// Validate this is not a duplicated request from Twilio
		Confirmation c = confirmationSvc.getByPurchaseOrderId(orderId);
		if (c.getStatus() != Status.REQUESTED) {
			System.out.println("Dupe status update from Twilio. " + orderId);
			return;
		}

		if (smsStatus.equals("sent")) {
			System.out.println("Got SMS from Twilio: " + smsStatus);

			c.setStatus(Status.DELIVERED);
		} else {
			c.setStatus(Status.UNDELIVERABLE);
			// p.fail(FailReason.UNCONFIRMABLE);
			// transactionSvc.update(p);
		}

		c.setStatusLastModifiedOn(new Date());
		confirmationSvc.update(c);
	}

	private void postPhoneConfirmation(String userId, Long phone)
			throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		Credentials credentials = new UsernamePasswordCredentials("hamilton",
				"Wi11u|$hit53$Bs4m3");
		httpClient.getCredentialsProvider().setCredentials(
				org.apache.http.auth.AuthScope.ANY, credentials);
		ClientExecutor clientExecutor = new ApacheHttpClient4Executor(
				httpClient);
		ClientRequestFactory fac = new ClientRequestFactory(clientExecutor,
				new URI(userBaseUrl));
		ClientRequest request = fac.createRequest(phoneVerifyURL + userId + "/"
				+ phone);
		ClientResponse cr = request.post();

		System.out.println("Phone verification response status code: "
				+ cr.getStatus());

	}

	public void setIouOrderSvc(IouOrderSvc iouOrderSvc) {
		this.iouOrderSvc = iouOrderSvc;
	}

	public void setConfirmationSvc(ConfirmationSvc confirmationSvc) {
		this.confirmationSvc = confirmationSvc;
	}

	public void setPhoneConfirmationSvc(
			PhoneConfirmationSvc phoneConfirmationSvc) {
		this.phoneConfirmationSvc = phoneConfirmationSvc;
	}

	public void setPaymentSvc(PaymentSvc paymentSvc) {
		this.paymentSvc = paymentSvc;
	}

	public void setUserOnboardSvc(UserOnboardSvcImpl userOnboardSvc) {
		this.userOnboardSvc = userOnboardSvc;
	}

}
/*
@POST
@Path("status/phone/{userId}")
public void postPhoneStatus(@PathParam("userId") String userId,
		@FormParam("SmsStatus") String smsStatus) {

	// Validate this is not a duplicated request from Twilio
	PhoneConfirmation pc = phoneConfirmationSvc.getByUserId(userId);
	if (pc.getStatus() != PhoneConfirmation.Status.REQUESTED) {
		System.out
				.println("Dupe phone verification status update from Twilio. "
						+ userId);
		return;
	}

	if (smsStatus.equals("sent")) {
		System.out.println("Got Phone Verification SMS from Twilio: "
				+ smsStatus);

		pc.setStatus(PhoneConfirmation.Status.DELIVERED);
	} else {
		pc.setStatus(PhoneConfirmation.Status.UNDELIVERABLE);
		// p.fail(FailReason.UNCONFIRMABLE);
		// transactionSvc.update(p);
	}

	pc.setStatusLastModifiedOn(new Date());
	phoneConfirmationSvc.update(pc);
}
*/
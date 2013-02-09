package ly.dollar.tx.svc;

import java.math.BigDecimal;
import java.net.URI;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

import com.twilio.sdk.TwilioRestClient;

import ly.dollar.tx.CurrencyUtils;
import ly.dollar.tx.dao.UserMessageDao;
import ly.dollar.tx.dao.UserTotalsDao;
import ly.dollar.tx.entity.DollarlyAnonPhoneResponse;
import ly.dollar.tx.entity.DollarlyUserResponse;
import ly.dollar.tx.entity.IouOrder;
import ly.dollar.tx.entity.LedgerTotals;
import ly.dollar.tx.entity.Sms;
import ly.dollar.tx.entity.UserMessage;

public class UserOnboardSvcImpl extends MessageService implements
		UserOnboardSvc {

	// MAKE BACKWARDS COMPATIBLE
	
	
	public static final String twitterCreateUrl = "https://jefferson-dit.rhcloud.com/DITShift/api/v3/dollarly/users/twitter/";
	public static final String smsCreateUrl = "https://thomas-currensea.rhcloud.com/jefferson/user/new/";
	public static final String phoneCreateUrl = "https://jefferson-dit.rhcloud.com/DITShift/api/v3/dollarly/users/phone/";
	public static final String userBaseUrl = "https://thomas-currensea.rhcloud.com/jefferson/user/";

	private IouOrderSvc iouOrderSvc;
	private final UserMessageDao userMessageDao;
	private final UserTotalsDao userTotalsDao;
	
	public UserOnboardSvcImpl(String twilioAccountSid, String twilioAuthToken,
			String fromPhone, UserMessageDao userMessageDao,
			UserTotalsDao userTotalsDao) {
		TwilioRestClient client = new TwilioRestClient(twilioAccountSid,
				twilioAuthToken);
		this.smsFactory = client.getAccount().getSmsFactory();
		this.fromPhone = fromPhone;
		this.userMessageDao = userMessageDao;
		this.userTotalsDao = userTotalsDao;
	
	}
	
	public void sendModalReminder(String message, Long to, Long from){
		this.sendSms(to, message);
	}

	
	//			CHANGE THE STATES -- BACKWARDS COMPATIBLE
	
	
	
	public void sendOnboardingMessage(IouOrder iou, Long payerPhone, Long payeePhone) {
		String payeeMessage;
		String payerMessage;
		if (iou.getPayeeFundingStatus().equals("NEW_PHONE")) {
			//payeeMessage = this.makeNewPhoneUserMessage(iou, false);
			payeeMessage = this.createNewPhoneUserMessage(iou, false);
		} else if (iou.getPayeeFundingStatus().equals("DWOLLA_FULL") ||
				iou.getPayeeFundingStatus().equals("PAYPAL_FULL")) {
			
			//payeeMessage = this.makeDwollaPartialMessage(iou, false);
			payeeMessage = this.createFSUserMessage(iou, false);
		} else {
			
		
			payeeMessage = this.createAuthedUserMessage(iou, false);
		}
		
		if (iou.getPayerFundingStatus().equals("NEW_PHONE")) {
			
			payerMessage = this.createNewPhoneUserMessage(iou, true);
		} else if (iou.getPayerFundingStatus().equals("DWOLLA_FULL")||
				iou.getPayerFundingStatus().equals("PAYPAL_FULL")) {
	
			
			payerMessage = this.createFSUserMessage(iou, true);
		} else {
			
			payerMessage = this.createAuthedUserMessage(iou, true);
		}
		
		System.out.println("PayeE Onboarding Message: [" + payeeMessage +"] " +
		" going to: " + payeePhone.toString());
		this.sendSms(payeePhone, payeeMessage);
		
		System.out.println("PayeR Onboarding Message: [" + payerMessage +"] " +
				" going to: " + payerPhone.toString());
		this.sendSms(payerPhone, payerMessage);
		
	}
	
	private String createNewPhoneUserMessage(IouOrder iou, boolean payer){
		
		//String usa = CurrencyUtils.formatDecimal(iou.getAmount());
		if (payer){
			UserMessage m = userMessageDao.findByType("NEWPHONE_PAYER");
			String message = m.getMessageBody();
			String amount = message.replaceFirst("@amount", iou.getAmount().toPlainString());
			String payee =  amount.replaceAll("@payee", iou.getPayeeHandle());
			String hashtag = payee.replaceAll("@hashtag", iou.getHashtag());
			String last = hashtag.replaceAll("@payer", iou.getPayerHandle());
			return last;
		} else {
			UserMessage m = userMessageDao.findByType("NEWPHONE_PAYEE");
			
			String message = m.getMessageBody();
			String amount = message.replaceFirst("@amount", iou.getAmount().toPlainString());
			String payee = amount.replaceAll("@payee", iou.getPayeeHandle());
			String hashtag = payee.replaceAll("@hashtag", iou.getHashtag());
			String p = hashtag.replaceAll("@payer", iou.getPayerHandle());
			//String b = CurrencyUtils.formatDecimal((iouOrderSvc.getOpenIouTotalByPhone(iou.getMention(), 
					//"payee")));
			LedgerTotals lt = userTotalsDao.findByPhone(Long.valueOf(iou.getMention()));
			if(lt == null)
			{	System.out.println("NEW_PHONE ---- NO Totals to update for iou: " +iou.toString());
				return p;
			}else
			{
				String last = p.replaceFirst("@total", 
						lt.getOpenCollects().toPlainString());
				return last;
			}
		
		}
	}
	
	private String createAuthedUserMessage(IouOrder iou, boolean payer){
		//String usa = CurrencyUtils.formatDecimal(iou.getAmount());

		if (payer){
			UserMessage m = userMessageDao.findByType("AUTHED_PAYER");
			String message = m.getMessageBody();
			String amount = message.replaceFirst("@amount", iou.getAmount().toPlainString());
			String payee =  amount.replaceAll("@payee", iou.getPayeeHandle());
			String hashtag = payee.replaceAll("@hashtag", iou.getHashtag());
			String last = hashtag.replaceAll("@payer", iou.getPayerHandle());
			return last;
		} else {
			UserMessage m = userMessageDao.findByType("AUTHED_PAYEE");
			String message = m.getMessageBody();
			String amount = message.replaceFirst("@amount", iou.getAmount().toPlainString());
			String payee = amount.replaceAll("@payee", iou.getPayeeHandle());
			String hashtag = payee.replaceAll("@hashtag", iou.getHashtag());
			String p = hashtag.replaceAll("@payer", iou.getPayerHandle());
			//String b = CurrencyUtils.formatDecimal((iouOrderSvc.getOpenIouTotalByPhone(iou.getMention(), 
					//"payee")));
			LedgerTotals lt = userTotalsDao.findByUserId(iou.getPayeeUserId());
			if(lt == null)
			{	System.out.println("AUTHED +++ NO Totals to update for iou: " +iou.toString());
				return p;
			}
			else
			{
				String last = p.replaceFirst("@total", 
						lt.getOpenCollects().toPlainString());
				return last;
			}
		}
	}
	private String createFSUserMessage(IouOrder iou, boolean payer){
		//String usa = CurrencyUtils.formatDecimal(iou.getAmount());

		if (payer){
			UserMessage m = userMessageDao.findByType("FS_PARTIAL_PAYER");
			String message = m.getMessageBody();
			String amount = message.replaceFirst("@amount",iou.getAmount().toPlainString());
			String payee =  amount.replaceAll("@payee", iou.getPayeeHandle());
			String hashtag = payee.replaceAll("@hashtag", iou.getHashtag());
			String last = hashtag.replaceAll("@payer", iou.getPayerHandle());
			
			return last;
		} else {
			UserMessage m = userMessageDao.findByType("FS_PARTIAL_PAYEE");
			String message = m.getMessageBody();
			String amount = message.replaceFirst("@amount", iou.getAmount().toPlainString());
			String payee =  amount.replaceAll("@payee", iou.getPayeeHandle());
			String hashtag = payee.replaceAll("@hashtag", iou.getHashtag());
			String p = hashtag.replaceAll("@payer", iou.getPayerHandle());
			
			String last = p.replaceFirst("@total", 
					userTotalsDao.findByUserId(iou.getPayeeUserId()).getOpenCollects().toPlainString());
			return last;
		}
	}
	
	private String generateUserMessage(IouOrder iou, UserMessage um){
		return null;
	}

	public DollarlyUserResponse create(String userName, Long id)
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
       ClientRequest request = fac.createRequest(twitterCreateUrl + userName
                       + "?sysId=" + id.toString());
       ClientResponse<DollarlyUserResponse> dur = request
                       .put(DollarlyUserResponse.class);
       DollarlyUserResponse d = dur.getEntity();
       System.out.println("User Onboarded:" + d.toString());
       return d;
	}


	public DollarlyAnonPhoneResponse createAnonPhone(Sms sms, String entity)
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
		ClientRequest request = null;
		if (entity == "payer")
			request = fac.createRequest(smsCreateUrl + sms.getSenderPhone());
		else if (entity == "payee")
			request = fac.createRequest(smsCreateUrl + sms.getReceiverPhone());

		ClientResponse<DollarlyAnonPhoneResponse> dur = request
				.put(DollarlyAnonPhoneResponse.class);
		DollarlyAnonPhoneResponse d = dur.getEntity();
		return d;
	}


	public void setIouOrderSvc(IouOrderSvc iouOrderSvc) {
		this.iouOrderSvc = iouOrderSvc;
	}



}

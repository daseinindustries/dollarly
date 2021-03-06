package ly.dollar.tx.svc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.builder.ToStringBuilder;

import ly.dollar.tx.CurrencyUtils;
import ly.dollar.tx.dao.ConfirmationDao;
import ly.dollar.tx.dao.UserMessageDao;
import ly.dollar.tx.entity.Confirmation;
import ly.dollar.tx.entity.ExtSystem;
import ly.dollar.tx.entity.ExtSystemEntity;
import ly.dollar.tx.entity.IouOrder;
import ly.dollar.tx.entity.UserMessage;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Sms;

public class ConfirmationSvc {
	private final SmsFactory smsFactory;
	private final String fromPhone;
	private final ConfirmationDao confirmationDao;
	private final String statusCallbackUrl;
	private final UserMessageDao userMessageDao;

	public ConfirmationSvc(String twilioAccountSid, String twilioAuthToken,
			String fromPhone, String statusCallbackUrl,
			ConfirmationDao confirmationDao, UserMessageDao userMessageDao) {
		TwilioRestClient client = new TwilioRestClient(twilioAccountSid,
				twilioAuthToken);
		this.smsFactory = client.getAccount().getSmsFactory();
		this.fromPhone = fromPhone;
		this.statusCallbackUrl = statusCallbackUrl;
		this.confirmationDao = confirmationDao;
		this.userMessageDao = userMessageDao;
	}

	public void request(Long phone, IouOrder iou, Long payeePhone) {
		int code = generateCode(phone);
		String body = createRequestMessage(iou, code);
		String url = statusCallbackUrl + iou.getId();
		System.out.println("BEFORE SMS details before new Confirmation: ");
		MessageDetails details = this.sendSms(phone, body, url);
		System.out.println("AFTER SMS details before new Confirmation: "
				+ iou.toString());
		Confirmation c = new Confirmation();
		c.setCode(code);
		c.setPhone(phone);
		c.setPayeePhone(payeePhone);
		c.setOrderId(iou.getId());
		c.setPayerUserId(iou.getPayerUserId());
		c.setPayeeUserId(iou.getPayeeUserId());
		c.setExtSystem(details.getExtSystem());
		c.setExtSystemId(details.getExtSystemId());
		c.setExtSystemDate(details.getExtSystemDate());
		c.setCreatedOn(new Date());
		c.setMessage(body);
		System.out.println("CREATING CONFIRMATION: " + c.toString());
		confirmationDao.create(c);
	}
	private String createRequestMessage(IouOrder iou, int code){
		//String usa = CurrencyUtils.formatDecimal(iou.getAmount());
		UserMessage m = userMessageDao.findByType("PAYMENT_CONFIRMATION");
		String ms = m.getMessageBody();
		String amount = ms.replaceFirst("@amount", iou.getAmount().toPlainString());
		String payee = amount.replaceAll("@payee", iou.getPayeeHandle());
		String hashtag = payee.replaceAll("@hashtag", iou.getHashtag());
		String end = hashtag.replaceAll("@code", String.valueOf(code));
		String payer = end.replaceFirst("@payer", iou.getPayerHandle());
		
		return payer;
	}
	
	public String createReceipt(IouOrder iou, boolean payer){
		//String usa = CurrencyUtils.formatDecimal(iou.getAmount());
		if(payer){
			UserMessage m = userMessageDao.findByType("PAYER_RECEIPT");
			String ms = m.getMessageBody();
			String amount = ms.replaceFirst("@amount",iou.getAmount().toPlainString());
			String payee = amount.replaceAll("@payee", iou.getPayeeHandle());
			String hashtag = payee.replaceAll("@hashtag", iou.getHashtag());
			String end = hashtag.replaceAll("@payer", iou.getPayerHandle());
			
			return end;
		} else {
			UserMessage m = userMessageDao.findByType("PAYEE_RECEIPT");
			String ms = m.getMessageBody();
			String amount = ms.replaceFirst("@amount", iou.getAmount().toPlainString());
			String payee = amount.replaceAll("@payee", iou.getPayeeHandle());
			String hashtag = payee.replaceAll("@hashtag", iou.getHashtag());
			String end = hashtag.replaceAll("@payer", iou.getPayerHandle());
			return end;
		}
	}

	public void createAndSendUnknownUserMessage(Long phone, IouOrder i, String unknownUser){
		UserMessage m = userMessageDao.findByType("UNKOWN_USER");
		String ms = m.getMessageBody();
		String amount = ms.replaceFirst("@amount",i.getAmount().toPlainString());
		String payee = amount.replaceAll("@payer", i.getPayerHandle());
		String hashtag = payee.replaceFirst("@hashtag", i.getHashtag());
		String end = hashtag.replaceFirst("@unknown", unknownUser);
		this.sendSms(phone, end);
	
	}
	public void createAndSendFailureMessage(Long phone, int code){
		UserMessage m = userMessageDao.findByType("PAY_CONF_INVALID_CODE");
		String ms = m.getMessageBody();
		String message = ms.replaceAll("@code", String.valueOf(code));
		String pm = message.replaceFirst("@phone", String.valueOf(phone));
		this.sendSms(phone, pm);
	}
	

	public void createAndSendExpireFailureMessage(Long phone, int code, IouOrder i){
		UserMessage m = userMessageDao.findByType("PAY_CONF_EXPIRED_CODE");
		String ms = m.getMessageBody();
		String message = ms.replaceAll("@code", String.valueOf(code));
		String pm = message.replaceFirst("@phone", String.valueOf(phone));
		this.sendSms(phone, pm);
	}
	
	public void createAndSendInvalidDwollaPin(Long phone, IouOrder i){
		UserMessage m = userMessageDao.findByType("DWOLLA_FAIL_INVALID_PIN");
		String ms = m.getMessageBody();
		String amount = ms.replaceFirst("@amount", i.getAmount().toPlainString());
		String payee = amount.replaceAll("@payee", i.getPayeeHandle());
		String hashtag = payee.replaceAll("@hashtag", i.getHashtag());
		String end = hashtag.replaceAll("@payer", i.getPayerHandle()); 
		this.sendSms(phone, end);
	}

	public void createAndSendInsufficientFundsDwolla(Long phone, IouOrder i){
		UserMessage m = userMessageDao.findByType("DWOLLA_FAIL_LACKS_FUNDING");
		String ms = m.getMessageBody();
		String amount = ms.replaceFirst("@amount", i.getAmount().toPlainString());
		String payee = amount.replaceAll("@payee", i.getPayeeHandle());
		String hashtag = payee.replaceAll("@hashtag", i.getHashtag());
		String end = hashtag.replaceAll("@payer", i.getPayerHandle()); 
		this.sendSms(phone, end);
		
	}
	
	public void createAndSendDwollaOther(Long phone, IouOrder i){
		UserMessage m = userMessageDao.findByType("DWOLL_FAIL_OTHER");
		String ms = m.getMessageBody();
		String amount = ms.replaceFirst("@amount", i.getAmount().toPlainString());
		String payee = amount.replaceAll("@payee", i.getPayeeHandle());
		String hashtag = payee.replaceAll("@hashtag", i.getHashtag());
		String end = hashtag.replaceAll("@payer", i.getPayerHandle()); 
		this.sendSms(phone, end);
		
	}
	
	public MessageDetails sendSms(Long phone, String body) {
		Map<String, String> params = baseSmsParams(phone, body);
		Sms sms = sendSms(params);
		return makeMessageDetails(sms);
	}

	public MessageDetails sendSms(Long phone, String body, String callbackUrl) {
		Map<String, String> params = baseSmsParams(phone, body);
		params.put("StatusCallback", callbackUrl);
		Sms sms = sendSms(params);
		return makeMessageDetails(sms);
	}

	private Sms sendSms(Map<String, String> params) {
		try {
			return smsFactory.create(params);
		} catch (TwilioRestException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> baseSmsParams(Long phone, String body) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("To", phone.toString());
		params.put("From", fromPhone);
		params.put("Body", body);

		return params;
	}

	private MessageDetails makeMessageDetails(Sms sms) {
		MessageDetails messageDetails = new MessageDetails();
		messageDetails.setExtSystem(ExtSystem.TWILIO);
		messageDetails.setExtSystemDate(sms.getDateCreated());
		messageDetails.setExtSystemId(sms.getSid());
		messageDetails.sent = true;
		return messageDetails;
	}
	public static class MessageDetails extends ExtSystemEntity {
		public boolean sent = false;
	}
	
	public String toString(ConfirmationSvc.MessageDetails md) {

		return ToStringBuilder.reflectionToString(md);

	}

	private int generateCode(long phone) {
		int code = 0;
		int attempts = 0;
		do {
			code = new Random().nextInt(9999 - 1000 + 1) + 1000;
			attempts++;
		} while (attempts < 10
				&& confirmationDao.findByCodePhone(code, phone) != null);

		return code;
	}

	

	public Confirmation getByPurchaseOrderId(String purchaseOrderId) {
		return confirmationDao.findByPurchaseOrderId(purchaseOrderId);
	}

	public void create(Confirmation c) {
		confirmationDao.create(c);
	}

	public void update(Confirmation c) {
		confirmationDao.update(c);
	}

	
	public Confirmation createConfirmationForFSUsers(IouOrder i) {
		Confirmation c = new Confirmation();
		c.setOrderId(i.getId());
		c.setPayerUserId(i.getPayerUserId());
		c.setPayeeUserId(i.getPayeeUserId());
		confirmationDao.create(c);
		return c;
	}

	public Confirmation getByPhoneCode(Long phone, Integer code) {
		return confirmationDao.findByCodePhone(code, phone);
	}

	public Confirmation confirm(Long phone, Integer code) {
		return confirmationDao.findAndConfirmByCodePhone(code, phone);

	}

}

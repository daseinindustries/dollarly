package ly.dollar.tx.svc;

import java.util.HashMap;
import java.util.Map;

import ly.dollar.tx.entity.ExtSystem;
import ly.dollar.tx.entity.ExtSystemEntity;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Sms;

public abstract class MessageService {
	
	protected SmsFactory smsFactory;
	protected String fromPhone;
	
	public MessageService(){
		smsFactory = null;
		fromPhone = null;
	}
	public MessageService(SmsFactory factory, String phone){
		this.smsFactory = factory;
		this.fromPhone = phone;
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
	
	public String toString(MessageService.MessageDetails md) {

		return ToStringBuilder.reflectionToString(md);

	}

}

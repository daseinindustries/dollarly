package ly.dollar.tx.svc;

import java.util.Date;
import java.util.Random;

import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;

import ly.dollar.tx.dao.PhoneConfirmationDao;
import ly.dollar.tx.dao.UserMessageDao;
import ly.dollar.tx.entity.PhoneConfirmation;
import ly.dollar.tx.entity.UserMessage;

import com.twilio.sdk.TwilioRestClient;

public class PhoneConfirmationSvc extends MessageService {

	private final PhoneConfirmationDao phoneConfirmationDao;
	private final String statusCallbackUrl;
	private final UserMessageDao userMessageDao;

	public PhoneConfirmationSvc(String twilioAccountSid,
			String twilioAuthToken, String fromPhone, String statusCallbackUrl,
			PhoneConfirmationDao phoneConfirmationDao,
			UserMessageDao userMessageDao) {
		TwilioRestClient client = new TwilioRestClient(twilioAccountSid,
				twilioAuthToken);
		this.smsFactory = client.getAccount().getSmsFactory();
		this.fromPhone = fromPhone;
		this.statusCallbackUrl = statusCallbackUrl;
		this.phoneConfirmationDao = phoneConfirmationDao;
		this.userMessageDao = userMessageDao;
	}

	public void request(Long phone, String userId) {
		int code = generateCode(phone);
		String body = createRequestMessage(userId, code);
		String url = statusCallbackUrl + "phone/" + userId;
		System.out
				.println("PhoneConfirmation: BEFORE SMS details before new PhoneConfirmation:");
		PhoneConfirmation pc = phoneConfirmationDao.findByUserId(userId);
		//first timer
		if (pc == null) 
		{
			this.createAndSendMessage(phone, body, url, userId, code);
		
		}
		else 
		{  // re-confirms/updates
			if(pc.getStatus().equals(PhoneConfirmation.Status.REQUESTED))
			{
				//resend old confirmation
				if(pc.getPhone().equals(phone)){
					System.out.println("Re-send SMS Confirmation");
					this.sendSms(phone, pc.getMessage(), url);
				}
				else //INVALID-ate old confirmation - send new one
				{
					System.out.println("INVALIDATE and send new code");
					pc.setStatus(PhoneConfirmation.Status.INVALID);
					phoneConfirmationDao.update(pc);
					this.createAndSendMessage(phone, body, url, userId, code);
					
				}
			}
			
		}
	}
	
	//requires serialization of status [PAYPAL_FULL --> FB_AUTHED_AND_PHONE --> PAYPAL_FULL]
	public void requestNewPhoneConfirmation(Long newPhone, String userId, String userStatus)
	{
		int code = generateCode(newPhone);
		String body = createRequestMessage(userId, code);
		String url = statusCallbackUrl + "phone/" + userId;
		System.out
				.println("NEW/UPDATE PhoneConfirmation");
		PhoneConfirmation pc = phoneConfirmationDao.findByUserIdStatus(userId, PhoneConfirmation.Status.CONFIRMED);
		if(pc == null)
		{
			System.out
			.println("ERROR in NEW/UPDATE PhoneConfirmation userId = " + userId);
		}
		else
		{
			System.out.println("UPDATE - INVALIDATE old phone"+pc.getPhone()+"and send new code");
			pc.setStatus(PhoneConfirmation.Status.INVALID);
			phoneConfirmationDao.update(pc);
			this.createAndSendMessageWithUserStatus(newPhone, body, url, userId, code, userStatus);
		}
	}
	
	private void createAndSendMessageWithUserStatus(Long phone, String body, String url, String userId, int code, String userStatus){
		
		// Send SMS
		MessageDetails details = this.sendSms(phone, body, url);
		PhoneConfirmation pc = new PhoneConfirmation();
		// Persist Record
		pc.setUserId(userId);
		pc.setPhone(phone);
		pc.setCode(code);
		pc.setExtSystem(details.getExtSystem());
		pc.setExtSystemId(details.getExtSystemId());
		pc.setExtSystemDate(details.getExtSystemDate());
		pc.setExtSystemNotes(userStatus);
		pc.setCreatedOn(new Date());
		pc.setMessage(body);
		System.out.println("CREATING PHONE CONFIRMATION: " + pc.toString());
		phoneConfirmationDao.create(pc);
	}
	
	private void createAndSendMessage(Long phone, String body, String url, String userId, int code){
		
		// Send SMS
		MessageDetails details = this.sendSms(phone, body, url);
		PhoneConfirmation pc = new PhoneConfirmation();
		// Persist Record
		pc.setUserId(userId);
		pc.setPhone(phone);
		pc.setCode(code);
		pc.setExtSystem(details.getExtSystem());
		pc.setExtSystemId(details.getExtSystemId());
		pc.setExtSystemDate(details.getExtSystemDate());
		pc.setCreatedOn(new Date());
		pc.setMessage(body);
		System.out.println("CREATING PHONE CONFIRMATION: " + pc.toString());
		phoneConfirmationDao.create(pc);
	}

	private String createRequestMessage(String userId, int code) {
		UserMessage m = userMessageDao.findByType("PHONE_CONFIRMATION");
		String ms = m.getMessageBody();
		String message = ms.replaceAll("@code", String.valueOf(code));
		return message;
	}

	public String createConfirmMessage(Long phone) {
		UserMessage m = userMessageDao.findByType("PHONE_CONFIRMED");
		String ms = m.getMessageBody();
		String message = ms.replaceFirst("@phone", String.valueOf(phone));
		return message;
	}

	public void createAndSendFailureMessage(Long phone, int code,
			String failType) {
		UserMessage m = userMessageDao.findByType(failType);
		String ms = m.getMessageBody();
		String message = ms.replaceAll("@code", String.valueOf(code));
		String pm = message.replaceFirst("@phone", String.valueOf(phone));
		this.sendSms(phone, pm);

	}

	public void createAndSendConfirmMessage(Long phone) {
		String message = this.createConfirmMessage(phone);
		this.sendSms(phone, message);
	}

	private int generateCode(Long phone) {
		int code = 0;
		int attempts = 0;
		do {
			code = new Random().nextInt(99999 - 10000 + 1) + 10000;
			attempts++;
		} while (attempts < 10
				&& phoneConfirmationDao.findByCodePhone(code, phone) != null);

		return code;
	}

	public PhoneConfirmation getByUserId(String userId) {
		return phoneConfirmationDao.findByUserId(userId);
	}

	public void create(PhoneConfirmation c) {
		phoneConfirmationDao.create(c);
	}

	public void update(PhoneConfirmation c) {
		phoneConfirmationDao.update(c);
	}

	public PhoneConfirmation getByPhoneCode(Long phone, Integer code) {
		return phoneConfirmationDao.findByCodePhone(code, phone);
	}

	public PhoneConfirmation confirm(Long phone, Integer code) {
		return phoneConfirmationDao.findAndConfirmByCodePhone(code, phone);

	}
}

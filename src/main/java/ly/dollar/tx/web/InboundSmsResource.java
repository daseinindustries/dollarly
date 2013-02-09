package ly.dollar.tx.web;

import java.util.UUID;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.jboss.resteasy.annotations.Form;



import ly.dollar.tx.entity.Sms;
import ly.dollar.tx.entity.SmsPostRequest;
import ly.dollar.tx.svc.SmsSvc;


@Path("sms")
public class InboundSmsResource {
	private SmsSvc smsSvc;

	
	public void setSmsSvc(SmsSvc smsSvc) {
		this.smsSvc = smsSvc;
	}
	
	
	
	//from Johnson ---------------------------------->>>>
	@POST
	public void postSms(@Form SmsPostRequest spr){
		System.out.println("SMS ------>>>> " + spr.toString());
		UUID uid = UUID.randomUUID();
		String smsId = spr.getId();
		String uuid = smsId+uid.toString();
		spr.setId(uuid);
		Sms sms = new Sms(spr);
		
		
		if(smsSvc.getBySmsThreadIds(spr.getId(), spr.getTid()) == null)
		{
			smsSvc.create(sms);
		}
		else 
		{
			System.out.println("SMS IS DUPLICATE! " + spr.toString());
		}
	}
	
	
	

	
	
}

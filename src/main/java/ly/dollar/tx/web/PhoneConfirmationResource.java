package ly.dollar.tx.web;


import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


import ly.dollar.tx.svc.PhoneConfirmationSvc;

@Path("confirmation/phone")
public class PhoneConfirmationResource {

	private PhoneConfirmationSvc phoneConfirmationSvc;


	public void setPhoneConfirmationSvc(PhoneConfirmationSvc phoneConfirmationSvc) {
		this.phoneConfirmationSvc = phoneConfirmationSvc;
	}
	
	@POST
	@Path("{userId}/{phone}")
	public void postPhoneConfirmation(@PathParam("userId") String userId,
			@PathParam("phone") Long phone)
	{
		System.out.println("RESOURCE RECIEVED PHONE CONF: " + phone +
				" userId: " + userId);
		phoneConfirmationSvc.request(phone, userId);
	}
}

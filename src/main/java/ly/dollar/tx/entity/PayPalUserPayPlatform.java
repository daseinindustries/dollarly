package ly.dollar.tx.entity;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;


import ly.dollar.tx.entity.UserPlatform.UserPayPlatform;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PayPalUserPayPlatform extends UserPayPlatform {

	private static final long serialVersionUID = -5590954221555318069L;
	
	private Emails emails;
	private String firstName;
	private String lastName;
	private String phone;
	private String email;
	private String preApprovalKey;
	
	public Emails getEmails() {
		return emails;
	}


	public void setEmails(Emails emails) {
		this.emails = emails;
	}


	public String getFirstName() {
		return firstName;
	}


	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public String getLastName() {
		return lastName;
	}


	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	public String getPhone() {
		return phone;
	}


	public void setPhone(String phone) {
		this.phone = phone;
	}



	
	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}




	public String getPreApprovalKey() {
		return preApprovalKey;
	}


	public void setPreApprovalKey(String preApprovalKey) {
		this.preApprovalKey = preApprovalKey;
	}




	public static class Emails extends ArrayList<String>{
		private static final long serialVersionUID = 1L;
		
	}

}

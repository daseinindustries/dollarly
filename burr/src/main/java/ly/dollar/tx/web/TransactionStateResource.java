package ly.dollar.tx.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import ly.dollar.tx.entity.IouOrder;

import ly.dollar.tx.svc.ConfirmationSvc;
import ly.dollar.tx.svc.IouOrderSvc;

@Path("transaction")
public class TransactionStateResource {
	private IouOrderSvc iouOrderSvc;
	private ConfirmationSvc confirmationSvc;
	
	//time to make nice

	@PUT
	@Path("iou/{iouId}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response put(IouOrder i, @PathParam("iouId") String iouId) {
		Response.Status status;
		if (iouId.equals(i.getId())) {
			iouOrderSvc.create(i);
			status = Response.Status.CREATED;
		} else {
			status = Response.Status.BAD_REQUEST;
		}

		return Response.status(status).build();
	}

	@POST
	@Path("iou/{userId}/{phone}/{status}/handle/{handle}")
	public void postAnonPhoneAuthedUpdate(@PathParam("userId") String userId, @PathParam("phone")String phone, 
			@PathParam("status")String status,
			@PathParam("handle") String handle) {
			System.out.println("UPDATE ANON CALLED: " + handle);
			iouOrderSvc.updateAnonPhoneAuthStatus(phone, userId, status, handle);
			
		

	}
	
	@POST
	@Path("iou/{userId}/{status}/handle/{handle}")
	public void postStatusUpdate(@PathParam("userId") String userId,@PathParam("status") String status,
			@PathParam("handle") String handle) {
		System.out.println("UPDATE Status CALLED: " + handle);

			iouOrderSvc.updateUserStatus(userId, status, handle);
		

	}
	
	@POST
	@Path("iou/{userId}/{status}")
	public Response postFSUpdate(@PathParam("userId") String userId,@PathParam("status") String status) {
		System.out.println("UPDATE FS Dwolla CALLED: " + status);

			iouOrderSvc.updateUserFundingStatus(userId, status);
			
			return Response.status(Response.Status.OK).build();
		

	}
	
	//assumption here is two fully FS-ed users
	@POST
	@Path("iou/{txId}/status/{status}")
	@Produces("application/json")
	public IouOrder postTxAction(@PathParam("txId") String txId, @PathParam("status") String status, 
			@QueryParam("payerPhone") Long payerPhone, @QueryParam("payeePhone") Long payeePhone)
	{	
		IouOrder i;
		
		if(status.equals("pay") || status.equals("collect")){
			i = iouOrderSvc.updateStatusToConfirm(txId);
			if(i.getStatus() == IouOrder.Status.PENDING_CONFIRMATION){
				confirmationSvc.request(payerPhone, i, payeePhone);
				System.out.println("confirmation created!");
			}
			return i;
		} else if (status.equals("void")){
			i =iouOrderSvc.updateStatusToVoid(txId);
			return i;
		} else {
			throw new WebApplicationException(Response.Status.NOT_MODIFIED);
		}
		
	}
	
	@GET
	@Path("iou/{iouId}")
	@Produces("application/json")
	public IouOrder get(@PathParam("iouId") String iouId) {
		IouOrder i = iouOrderSvc.getById(iouId);
		if (i != null) {
			return i;
		}

		throw new WebApplicationException(Response.Status.NOT_FOUND);
	}

	public void setIouOrderSvc(IouOrderSvc iouOrderSvc) {
		this.iouOrderSvc = iouOrderSvc;
	}

	public void setConfirmationSvc(ConfirmationSvc confirmationSvc) {
		this.confirmationSvc = confirmationSvc;
	}

}

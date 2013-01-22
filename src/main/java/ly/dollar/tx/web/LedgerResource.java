package ly.dollar.tx.web;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import ly.dollar.tx.entity.IouOrder;
import ly.dollar.tx.entity.Ledger;
import ly.dollar.tx.entity.LedgerTotals;
import ly.dollar.tx.svc.IouOrderSvc;

@Path("ledger")
public class LedgerResource {
	
	private IouOrderSvc iouOrderSvc;
	
	
	@GET
	@Path("/{userId}")
	@Produces("application/json")
	public Ledger get(@PathParam("userId") String userId){
		
		Ledger ledger = new Ledger();
		
		Collection<IouOrder> collect = 
				iouOrderSvc.getByPayeeUserId(userId);
		Collection<IouOrder> pay =
				iouOrderSvc.getByPayerUserId(userId);
		ledger.setCollect(collect);
		ledger.setPay(pay);
	
		
		return ledger;
		
	}
	
	@GET
	@Produces("application/json")
	@Path("/phone/{phone}/totals/{iouStatus}")
	public LedgerTotals getTotalsByPhone(@PathParam("phone") String phone, 
			@PathParam("iouStatus") String iouStatus)
	{
		return iouOrderSvc.getOpenLedgerTotalsByPhone(phone);
	}
	
	@GET
	@Produces("application/json")
	@Path("/{userId}/totals/{iouStatus}")
	public LedgerTotals getTotalsByUserId(@PathParam("userId") String userId, 
			@PathParam("iouStatus") String iouStatus)
	{
		return iouOrderSvc.getOpenLedgerTotalsById(userId);
	}
	
	public void setIouOrderSvc(IouOrderSvc iouOrderSvc) {
		this.iouOrderSvc = iouOrderSvc;
	}
	
	
}

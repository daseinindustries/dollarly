package ly.dollar.tx.web;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import ly.dollar.tx.entity.IouOrder;
import ly.dollar.tx.entity.UserSpendingLimit;
import ly.dollar.tx.svc.IouOrderSvc;
import ly.dollar.tx.svc.PaymentSvc;

@Path("spending-limit")
public class UserSpendingLimitResource
{

    private IouOrderSvc iouOrderSvc;

    @GET
    @Path("{userId}")
    @Produces("application/json")
    public UserSpendingLimit get(@PathParam("userId") String userId,
    		@QueryParam("amount") BigDecimal amount)
    {
        UserSpendingLimit usl = new UserSpendingLimit();

        BigDecimal spent = BigDecimal.ZERO;
        for (IouOrder iou : iouOrderSvc.getPayerIousSince(userId, windowStartDate()))
        {
            if (iou.getStatus() == IouOrder.Status.PAID)
            {
                spent = spent.add(iou.getAmount());
                usl.addClearance(iou);
            }
        }

        usl.setProximity(PaymentSvc.SPENDING_LIMIT_AMOUNT.subtract(spent));
        if(amount != null){
            usl.calculateClearableDate(amount);
        }
        return usl;
    }

    public void setIouOrderSvc(IouOrderSvc iouOrderSvc)
    {
        this.iouOrderSvc = iouOrderSvc;
    }

    // TODO = Move this out into a DateUtil
    private static Date windowStartDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -PaymentSvc.SPENDING_LIMIT_WINDOW_DAYS);
        return cal.getTime();
    }

}

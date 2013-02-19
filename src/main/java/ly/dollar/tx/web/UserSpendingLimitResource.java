package ly.dollar.tx.web;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

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
    public UserSpendingLimit get(@PathParam("userId") String userId)
    {
        UserSpendingLimit usl = new UserSpendingLimit();
        
        List<IouOrder> openIous = new LinkedList<IouOrder>();
        
        // Create clearances first...
        BigDecimal spent = BigDecimal.ZERO;
        for (IouOrder iou : iouOrderSvc.getPayerIousSince(userId, windowStartDate()))
        {
            if (iou.getStatus() == IouOrder.Status.PAID)
            {
                spent = spent.add(iou.getAmount());
                usl.addClearance(iou);
            }
            
            if (iou.getStatus() == IouOrder.Status.OPEN)
            {
                openIous.add(iou);
            }
        }
        
        // .. then set proximity
        usl.setProximity(PaymentSvc.SPENDING_LIMIT_AMOUNT.subtract(spent));
        
        // .. and finally add the availabilities. (It's important to do this last.)
        for (IouOrder iou : openIous)
        {
            usl.addAvailablity(iou);
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

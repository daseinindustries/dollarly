package ly.dollar.tx.entity;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ly.dollar.tx.svc.PaymentSvc;

public class UserSpendingLimit
{
    // Hard limit details (May be per-user in the future?)
    private final BigDecimal limit = PaymentSvc.SPENDING_LIMIT_AMOUNT;
    private final Integer windowInDays = PaymentSvc.SPENDING_LIMIT_WINDOW_DAYS;
    
    // User's current status
    private BigDecimal proximity; // Dollars until limit is reached
    private final List<Clearance> clearances = new LinkedList<Clearance>();
    
    public BigDecimal getLimit()
    {
        return limit;
    }

    public Integer getWindowInDays()
    {
        return windowInDays;
    }

    public BigDecimal getProximity()
    {
        return proximity;
    }

    public void setProximity(BigDecimal proximity)
    {
        this.proximity = proximity;
    }

    public List<Clearance> getClearances()
    {
        return clearances;
    }

    public void addClearance(IouOrder iou)
    {
        clearances.add(new Clearance(iou));
    }

    /**
     * A 'Clearance' is an amount/date pair representing a point 
     * in time when the amount will no longer count towards the user's spending limit.
     */
    public static class Clearance
    {
        private BigDecimal amount;
        private Date clearsOn;

        public Clearance(IouOrder iou)
        {
            amount = iou.getAmount();
            clearsOn = addWindowDays(iou.getCreatedOn());
        }
        
        public BigDecimal getAmount()
        {
            return amount;
        }

        public void setAmount(BigDecimal amount)
        {
            this.amount = amount;
        }

        public Date getClearsOn()
        {
            return clearsOn;
        }

        public void setClearsOn(Date clearsOn)
        {
            this.clearsOn = clearsOn;
        }
        
        private Date addWindowDays(Date createdOn)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(createdOn);
            cal.add(Calendar.DATE, PaymentSvc.SPENDING_LIMIT_WINDOW_DAYS);
            return cal.getTime();
        }
    }
}

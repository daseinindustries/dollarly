package ly.dollar.tx.entity;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import ly.dollar.tx.svc.PaymentSvc;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class UserSpendingLimit
{
    // Hard limit details (May be per-user in the future?)
    private final BigDecimal limit = PaymentSvc.SPENDING_LIMIT_AMOUNT;
    private final Integer windowInDays = PaymentSvc.SPENDING_LIMIT_WINDOW_DAYS;
    
    // User's current status
    private BigDecimal proximity; // Dollars until limit is reached
    private final List<Clearance> clearances = new LinkedList<Clearance>();
    private final List<Availablity> availablities = new LinkedList<Availablity>();
    
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

	public List<Availablity> getAvailablities()
    {
        return availablities;
    }

	/*
	 * Warning! The availability of an Iou will be calculated 
	 * with the Clearances and proximity of the instance.
	 * I.e. add all Clearances before adding an Availability.
	 */
    public void addAvailablity(IouOrder iou)
    {
        availablities.add(new Availablity(iou, proximity, clearances));
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
    
    /**
     * A 'Availablity' is an iou/date pair representing a point 
     * in time when an open iou would be within the spending limits.
     */
    public static class Availablity
    {
        private final String iouId;
        private Date availableOn;

        public Availablity(IouOrder iou, BigDecimal proximity, List<Clearance> clearances)
        {
            iouId = iou.getId();
            init(iou, proximity, clearances);
        }

        public String getIouId()
        {
            return iouId;
        }

        public Date getAvailableOn()
        {
            return availableOn;
        }

        private void init(IouOrder iou, BigDecimal proximity, List<Clearance> clearances)
        {
            BigDecimal delta = proximity.subtract(iou.getAmount());
            if (delta.compareTo(BigDecimal.ZERO) == -1)
            {
                for (Clearance c : clearances)
                {
                    delta = delta.add(c.getAmount());
                    if (delta.compareTo(BigDecimal.ZERO) > -1)
                    {
                        this.availableOn = c.getClearsOn();
                        break;
                    }
                }
            } 
            else
            {
                this.availableOn = Calendar.getInstance().getTime();
            }
        }
    }

}

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
    private Date clearableDate;
    private BigDecimal forAmount;
    
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

	public BigDecimal getForAmount() {
		return forAmount;
	}

	public void setForAmount(BigDecimal amount) {
		this.forAmount = amount;
	}
	
    public Date getClearableDate() {
		return clearableDate;
	}

	public void setClearableDate(Date clearableDate) {
		this.clearableDate = clearableDate;
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

	public void calculateClearableDate(BigDecimal amount) {
		BigDecimal delta = this.proximity.subtract(amount);
		if(delta.compareTo(BigDecimal.ZERO) == -1)
		{
			//calculate
			for(Clearance c : this.clearances){
				delta = delta.add(c.getAmount());
				if(delta.compareTo(BigDecimal.ZERO) > -1)
				{
					this.forAmount = amount;
					this.clearableDate = c.getClearsOn();
					break;
				}
			}
			if(this.forAmount == null)
			{	
				//if we never find a date, return null date indicating NEVER
				this.forAmount = amount;
			}
		}
		else
		{
			//if zero or greater, set now()
			this.forAmount = amount;
			Calendar cal = Calendar.getInstance();
			this.clearableDate = cal.getTime();
		}
	}


}

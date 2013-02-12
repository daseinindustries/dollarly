package ly.dollar.tx.svc;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import ly.dollar.tx.dao.IouOrderDao;
import ly.dollar.tx.dao.UserTotalsDao;
import ly.dollar.tx.entity.IouOrder;
import ly.dollar.tx.entity.LedgerTotals;

public class IouOrderSvc
{
    private final IouOrderDao iouOrderDao;
    private final UserTotalsDao userTotalsDao;

    public IouOrderSvc(IouOrderDao iouOrderDao, UserTotalsDao userTotalsDao)
    {
        this.iouOrderDao = iouOrderDao;
        this.userTotalsDao = userTotalsDao;
    }

    public void create(IouOrder p)
    {
        iouOrderDao.create(p);
    }

    public void createSmsIou(IouOrder p)
    {
        iouOrderDao.create(p);
        updateOpenSmsTotals(p);
    }

    public void update(IouOrder p)
    {
        iouOrderDao.update(p);
    }

    public void updateUserFundingStatus(String userId, String status)
    {
        iouOrderDao.updateUserFundingStatus(userId, status);
    }

    public void updateUserStatus(String userId, String status, String handle)
    {
        iouOrderDao.updateUserStatus(userId, status, handle);
    }

    // update ledger if one exists, otherwise create one - artifact of cold start
    public void updateAnonPhoneAuthStatus(String phone, String userId,
            String status, String handle)
    {
        iouOrderDao.updateUserByPhone(phone, userId, status, handle);
        LedgerTotals lt = userTotalsDao.findByPhone(Long.valueOf(phone));
        if (lt == null)
        {
            lt = new LedgerTotals();
            lt.setPhone(Long.valueOf(phone));
            lt.setUserId(userId);
            userTotalsDao.create(lt);
        } else
        {
            lt.setUserId(userId);
            userTotalsDao.update(lt);
        }

    }

    /*
     * creating ledger totals below (when not found by phone) assumes there
     * exists a userId.
     * 
     * in the case of NEW_PHONE, no userId exists
     * 
     * thus, updated when user verifies phone
     * 
     * in above method
     */
    public void updateOpenSmsTotals(IouOrder iou)
    {
        LedgerTotals payerTotals = userTotalsDao.findByPhone(Long.parseLong(iou.getExtSystemUserName()));
        LedgerTotals payeeTotals = userTotalsDao.findByPhone(Long.parseLong(iou.getMention()));
        if (iou.getStatus().equals(IouOrder.Status.OPEN))
        {
            if (payerTotals != null)
            {
                BigDecimal pays = payerTotals.getOpenPays();
                BigDecimal up = pays.add(iou.getAmount());
                payerTotals.setOpenPays(up);
                userTotalsDao.update(payerTotals);
            } else
            {
                payerTotals = new LedgerTotals();
                payerTotals.setOpenPays(iou.getAmount());
                payerTotals.setPhone(Long.parseLong(iou.getExtSystemUserName()));
                payerTotals.setUserId(iou.getPayerUserId());
                userTotalsDao.create(payerTotals);
            }

            if (payeeTotals != null)
            {
                BigDecimal collects = payeeTotals.getOpenCollects();
                BigDecimal up = collects.add(iou.getAmount());
                payeeTotals.setOpenCollects(up);
                userTotalsDao.update(payeeTotals);
            } else
            {
                payeeTotals = new LedgerTotals();
                payeeTotals.setOpenCollects(iou.getAmount());
                payeeTotals.setPhone(Long.parseLong(iou.getMention()));
                payeeTotals.setUserId(iou.getPayeeUserId());
                userTotalsDao.create(payeeTotals);
            }
        } else if (iou.getStatus().equals(IouOrder.Status.VOID)
                || iou.getStatus().equals(IouOrder.Status.PAID)
                || iou.getStatus().equals(IouOrder.Status.FAILED))
        {
            if (payerTotals != null)
            {
                BigDecimal pays = payerTotals.getOpenPays();
                BigDecimal up = pays.subtract(iou.getAmount());
                payerTotals.setOpenPays(up);
                userTotalsDao.update(payerTotals);
            }
            if (payeeTotals != null)
            {
                BigDecimal collects = payeeTotals.getOpenCollects();
                BigDecimal up = collects.subtract(iou.getAmount());
                payeeTotals.setOpenCollects(up);
                userTotalsDao.update(payeeTotals);
            }
        }
    }

    public LedgerTotals getOpenLedgerTotalsByPhone(String phone)
    {
        return userTotalsDao.findByPhone(Long.parseLong(phone));
    }

    public LedgerTotals getOpenLedgerTotalsById(String userId)
    {
    	LedgerTotals l = userTotalsDao.findByUserId(userId);
    	l.setPaysThisWeek(this.getPayerPastPayTotalSince(userId, sevenDaysAgo()));
    	return l;
    }

    public BigDecimal getOpenIouTotalByUserId(String uid, String payParty)
    {
        if (payParty.equals("payee"))
        {
            LedgerTotals t = userTotalsDao.findById(uid);
            return t != null ? t.getOpenCollects() : BigDecimal.ZERO;
        } else
        {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getOpenIouTotalByPhone(String phone, String payParty)
    {
        if (payParty.equals("payee"))
        {
            LedgerTotals t = userTotalsDao.findByPhone(Long.parseLong(phone));
            return t != null ? t.getOpenCollects() : BigDecimal.ZERO;
        } else
        {
            return BigDecimal.ZERO;
        }
    }
    
    public BigDecimal getPayerPastPayTotalSince(String userId, Date onOrAfter)
    {
        return sumPaid (
            iouOrderDao.findByPayerUserIdAndCreateDate(userId, onOrAfter)
        );
    }
    
    public Collection<IouOrder> getPayerIousSince(String userId, Date onOrAfter)
    {
        return iouOrderDao.findByPayerUserIdAndCreateDate(userId, onOrAfter);
    }

    public IouOrder getById(String id)
    {
        return iouOrderDao.findById(id);
    }

    public IouOrder getByExternalSystemIdAndName(String id, String externalSystem)
    {
        return iouOrderDao.findByExternalSystemIdAndName(id, externalSystem);
    }

    public Collection<IouOrder> getByPayeeUserId(String payeeUserId)
    {
        return iouOrderDao.findByPayeeUserId(payeeUserId);
    }

    public Collection<IouOrder> getByPayerUserId(String payerUserId)
    {
        return iouOrderDao.findByPayerUserId(payerUserId);
    }

    public void remove(String id)
    {
        iouOrderDao.findAndRemoveById(id);
    }

    public IouOrder updateStatusToVoid(String id)
    {
        IouOrder i = iouOrderDao.updateStatusToVoid(id);
        this.updateOpenSmsTotals(i);
        return i;
    }

    public IouOrder updateStatusToConfirm(String id)
    {
        return iouOrderDao.updateStatusToConfirm(id);
    }

    private BigDecimal sumPaid(Collection<IouOrder> ious)
    {
        BigDecimal total = new BigDecimal("0");
        for (IouOrder o : ious)
        {
            if(o.getStatus() == IouOrder.Status.PAID) 
            {
                total = total.add(o.getAmount());
            }
        }
        return total;
    }
    public static Date sevenDaysAgo()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        return cal.getTime();
    }
}

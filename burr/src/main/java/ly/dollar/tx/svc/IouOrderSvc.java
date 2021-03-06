package ly.dollar.tx.svc;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import ly.dollar.tx.entity.IouOrder;
import ly.dollar.tx.entity.LedgerTotals;

import ly.dollar.tx.dao.IouOrderDao;
import ly.dollar.tx.dao.UserTotalsDao;

public class IouOrderSvc {

	private final IouOrderDao iouOrderDao;
	private final UserTotalsDao userTotalsDao;

	public IouOrderSvc(IouOrderDao iouOrderDao, UserTotalsDao userTotalsDao) {
		this.iouOrderDao = iouOrderDao;
		this.userTotalsDao = userTotalsDao;
	}

	public void create(IouOrder p) {
		iouOrderDao.create(p);
	}

	public void createSmsIou(IouOrder p) {
		iouOrderDao.create(p);
		this.updateOpenSmsTotals(p);

	}

	public void update(IouOrder p) {
		iouOrderDao.update(p);

	}

	public void updateUserFundingStatus(String userId, String status) {

		iouOrderDao.updateUserFundingStatus(userId, status);

	}

	public void updateUserStatus(String userId, String status, String handle) {
		iouOrderDao.updateUserStatus(userId, status, handle);
	}

	public void updateAnonPhoneAuthStatus(String phone, String userId,
			String status, String handle) {
		iouOrderDao.updateUserByPhone(phone, userId, status, handle);
	}

	public void updateOpenSmsTotals(IouOrder iou) {
		LedgerTotals payerTotals = userTotalsDao.findByPhone(Long.parseLong(iou
				.getExtSystemUserName()));
		
		LedgerTotals payeeTotals = userTotalsDao.findByPhone(Long.parseLong(iou
				.getMention()));
		if (iou.getStatus().equals(IouOrder.Status.OPEN)) {
			if (payerTotals != null) {
				BigDecimal pays = payerTotals.getOpenPays();
				BigDecimal up = pays.add(iou.getAmount());
				payerTotals.setOpenPays(up);
				userTotalsDao.update(payerTotals);
			} else {
				payerTotals = new LedgerTotals();
				payerTotals.setOpenPays(iou.getAmount());
				payerTotals.setPhone(Long.parseLong(iou.getExtSystemUserName()));
				payerTotals.setUserId(iou.getPayerUserId());
				userTotalsDao.create(payerTotals);
			}

			if (payeeTotals != null) {
				BigDecimal collects = payeeTotals.getOpenCollects();
				BigDecimal up = collects.add(iou.getAmount());
				payeeTotals.setOpenCollects(up);
				userTotalsDao.update(payeeTotals);
			} else {
				payeeTotals = new LedgerTotals();
				payeeTotals.setOpenCollects(iou.getAmount());
				payeeTotals.setPhone(Long.parseLong(iou.getMention()));
				payeeTotals.setUserId(iou.getPayeeUserId());
				userTotalsDao.create(payeeTotals);
			}
		} else if (iou.getStatus().equals(IouOrder.Status.VOID)
				|| iou.getStatus().equals(IouOrder.Status.PAID)
				|| iou.getStatus().equals(IouOrder.Status.FAILED)){
			if (payerTotals != null) {
				BigDecimal pays = payerTotals.getOpenPays();
				BigDecimal up = pays.subtract(iou.getAmount());
				payerTotals.setOpenPays(up);
				userTotalsDao.update(payerTotals);
			} 
			if (payeeTotals != null) {
				BigDecimal collects = payeeTotals.getOpenCollects();
				BigDecimal up = collects.subtract(iou.getAmount());
				payeeTotals.setOpenCollects(up);
				userTotalsDao.update(payeeTotals);
			}
		}
	}

	public LedgerTotals getOpenLedgerTotalsByPhone(String phone) {
		return userTotalsDao.findByPhone(Long.parseLong(phone));
	}

	public LedgerTotals getOpenLedgerTotalsById(String userId) {
		return userTotalsDao.findByUserId(userId);
	}

	public BigDecimal getOpenIouTotalByUserId(String uid, String payParty) {

		if (payParty.equals("payee")) {
			LedgerTotals t = userTotalsDao.findById(uid);
			if(t!=null){
				return t.getOpenCollects();
			} else{
				return BigDecimal.ZERO;

			}
		
		} else {
			return BigDecimal.ZERO;
		}
	}

	public BigDecimal getOpenIouTotalByPhone(String phone, String payParty) {

		if (payParty.equals("payee")) {
			LedgerTotals t = userTotalsDao.findByPhone(Long.parseLong(phone));
			if(t!=null){
				return t.getOpenCollects();
			} else{
				return BigDecimal.ZERO;

			}
		
		} else {
			return BigDecimal.ZERO;
		}
	}

	private BigDecimal calculateTotal(List<IouOrder> ious) {
		double t = 0.00;
		for (IouOrder o : ious) {
			t = t + o.getAmount().doubleValue();
		}
		return new BigDecimal(t);
	}

	public IouOrder getById(String id) {
		return iouOrderDao.findById(id);
	}

	public IouOrder getByExternalSystemIdAndName(String id,
			String externalSystem) {
		return iouOrderDao.findByExternalSystemIdAndName(id, externalSystem);
	}

	public Collection<IouOrder> getByPayeeUserId(String payeeUserId) {
		return iouOrderDao.findByPayeeUserId(payeeUserId);
	}

	public Collection<IouOrder> getByPayerUserId(String payerUserId) {
		return iouOrderDao.findByPayerUserId(payerUserId);
	}

	public void remove(String id) {
		iouOrderDao.findAndRemoveById(id);
	}

	public IouOrder updateStatusToVoid(String id) {
		IouOrder i = iouOrderDao.updateStatusToVoid(id);
		this.updateOpenSmsTotals(i);
		return i;
	}

	public IouOrder updateStatusToConfirm(String id) {

		IouOrder i = iouOrderDao.updateStatusToConfirm(id);
		return i;

	}

}

package ly.dollar.tx.svc;

import java.util.List;

import ly.dollar.tx.dao.SmsDao;

import ly.dollar.tx.entity.Sms;


public class SmsSvc {

	private final SmsDao smsDao;
	
	public SmsSvc(SmsDao smsDao)
	{
		this.smsDao = smsDao;
	}
	

	
	public void create(Sms p)
	{
		smsDao.create(p);
	}

	
	public void update(Sms p)
	{
		smsDao.update(p);
	}

	public Sms getBySmsThreadIds(String smsId, String tid)
	{
		return smsDao.findBySmsIdThreadId(smsId, tid);
	}

	public Sms getById(String id)
	{
		return smsDao.findById(id);
	}
	
	public List<Sms> getByStatus(String status){
		List<Sms> smss = smsDao.findByStatus(status);
		return smss;
	}

	public List<Sms> getUnprocessedToProcessing()
	{	
		List<Sms> smss = smsDao.getUnprocessedToProcessing();
		return smss;
		
	}
}

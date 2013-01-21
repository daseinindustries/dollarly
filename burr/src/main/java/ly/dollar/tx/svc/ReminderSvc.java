package ly.dollar.tx.svc;

import java.util.List;

import ly.dollar.tx.dao.ReminderDao;
import ly.dollar.tx.entity.Confirmation;
import ly.dollar.tx.entity.Reminder;


public class ReminderSvc {

	private final ReminderDao reminderDao;
	
	public ReminderSvc(ReminderDao reminderDao)
	{
		this.reminderDao = reminderDao;
	}
	
	public void create(Reminder r)
	{
		reminderDao.create(r);
	}

	public void update(Reminder r)
	{
		reminderDao.update(r);
	}
	
	public Reminder getById(String id)
	{
		return reminderDao.findById(id);
	}
	
	public Reminder getByEntityId(String entityId)
	{
		return reminderDao.findByEntityId(entityId);
	}
	
	public Reminder getByEntityAndMessageId(String entityId, String messageId)
	{
		return reminderDao.findByEntityIdAndMessageId(entityId, messageId);
	}
	
	public List<Reminder> getOpenReminders(){
	
		return reminderDao.findByStatus(Reminder.Status.OPEN);
	}
	
	public List<Reminder> getOpenToProcessing(){
		return reminderDao.updateOpenToProcessing(this.getOpenReminders());
	}

	public void createConfirmationReminder(Confirmation c, String messageId) {
		Reminder r = new Reminder();
		r.setEntity(Reminder.Entity.Confirmation);
		r.setEntityId(c.getId());
		r.setMessageId(messageId);
		r.setStatus(Reminder.Status.OPEN);
		reminderDao.create(r);
			
	}
}

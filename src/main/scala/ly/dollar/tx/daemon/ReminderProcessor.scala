package ly.dollar.tx.daemon
import scala.actors.Actor
import scala.actors.Futures._
import scala.collection.JavaConverters._

import java.lang.{ Long => JavaLong }
import java.util.Calendar

import ly.dollar.tx._
import ly.dollar.config._
import ly.dollar.tx.entity._
import ly.dollar.tx.svc._
import ly.dollar.tx.utils._

class ReminderProcessor(reminderSvc: ReminderSvc, actor: IouReminderActor) {

  private var status = AppStatus.RUNNING
  private val throttle = new Throttle(10000L)

  def start() { actor.start; future { process } }

  def stop() { status = AppStatus.STOPPING; while (status != AppStatus.STOPPED) {} }

  def test(r: Reminder) { actor.start; actor ! r }

  private def process {
    System.out.println("Polling for unprocessed Reminders.");
    while (status == AppStatus.RUNNING && throttle.throttle) {
    	val reminders = reminderSvc.getOpenToProcessing().asScala.toList
    	if(!reminders.isEmpty){
    	  System.out.println("Found REMINDERS to process. " + reminders.size)
    	  processList(reminders)
    	}
      if (status == AppStatus.STOPPING) status = AppStatus.STOPPED
    }
  }
  
  private def processList(reminders: List[Reminder])
  {
     System.out.println(reminders.size + " reminders fetched.");
     reminders.foreach(r => actor ! r)
  }
}

class IouReminderActor(iouOrderSvc: IouOrderSvc, confirmationSvc: ConfirmationSvc, reminderSvc: ReminderSvc, configSvc: ConfigSvc) extends Actor {
  def act {
    loop {
      react {
        case reminder: Reminder =>
          if(reminder.getEntity().equals(Reminder.Entity.Confirmation))
          {
            System.out.println("ReminderProcessor::IouReminderActor: reminder=" + reminder)
           
            val confirmation = confirmationSvc.getById(reminder.getEntityId())
            val cdate = confirmation.getCreatedOn()
            
            val cal = Calendar.getInstance
            cal.setTime(cdate);
            cal.add(Calendar.MINUTE, Integer.parseInt(configSvc.get("reminder.iou")));
            
            val now = Calendar.getInstance
            System.out.println("ReminderProcessor::IouReminderActor: confTime=" + cal.toString() + " now=" + now.toString());

            if(now.getTime().after(cal.getTime())){
              
            	System.out.println("ReminderProcessor::IouReminderActor: FIRE IT UP!");

            confirmationSvc.createAndSendPayeeLedgerMessage(iouOrderSvc.getById(confirmation.getOrderId()))
            reminder.setStatus(Reminder.Status.COMPLETE);
            reminderSvc.update(reminder)
            } else {
           System.out.println("ReminderProcessor::NOT READY!");

              reminder.setStatus(Reminder.Status.OPEN);
              reminderSvc.update(reminder);
            }
            
          }
      }
    }
  }
}
class Reminders() extends java.util.ArrayList[Reminder] {}


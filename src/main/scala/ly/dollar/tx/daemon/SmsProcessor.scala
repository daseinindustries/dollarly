package ly.dollar.tx.daemon
import scala.actors.Actor
import scala.actors.Futures._
import scala.collection.JavaConverters._

import java.lang.{ Long => JavaLong }
import java.math.BigDecimal
import org.springframework.social.twitter.api.Tweet
import org.springframework.social.twitter.api.Twitter

import ly.dollar.tx._
import ly.dollar.config._
import ly.dollar.tx.entity._
import ly.dollar.tx.svc._
import ly.dollar.tx.utils._

class SmsProcessor(actor: SmsActor, smsSvc: SmsSvc, configSvc: ConfigSvc) {

  private var status = AppStatus.RUNNING
  private val throttle = new Throttle(2000L)
  
  def start() { actor.start; future { process } }

  def stop() { status = AppStatus.STOPPING; while (status != AppStatus.STOPPED) {} }

  def test(s: Sms) { actor.start; actor ! s }

  private def process {
    System.out.println("Polling for unprocessed SMS Messages.");
    while (status == AppStatus.RUNNING && throttle.throttle) {
      val messages = smsSvc.getUnprocessedToProcessing().asScala.toList // does a find and modify to move it to PROCESSING
      if (!messages.isEmpty) {
        System.out.println("Found messages to process. " + messages.size)
        processList(messages)
      }
      if (status == AppStatus.STOPPING) status = AppStatus.STOPPED
    }
  }

  private def processList(smss: List[Sms]) {
    System.out.println(smss.size + " smss fetched.");
    smss.foreach(t => actor ! t)
  }

}

class SmsActor(iouOrderSvc: IouOrderSvc, userUrl: String, userNameUrl: String, listingUrl: String,
  smsSvc: SmsSvc, userOnboardSvc: UserOnboardSvc, confirmationSvc: ConfirmationSvc) extends Actor {
  


  def act {
    loop {
      react {
        case sms: Sms =>
          parse(sms) match {
            case Some(tx) =>
              /*
               * OLD PAYPAL LIMITATION 'FRONT DOOR'
               * 
            	val limit = new BigDecimal("25.00").setScale(2)
            	 else if (tx.getAmount().compareTo(limit) == 1)
              {
                confirmationSvc.createAndSendLimitNotAllowed(payerPhone, tx);
              }
              * 
              */
              val iou = new IouOrder(tx)
              var payerPhone: JavaLong = new JavaLong(sms.getSenderPhone());

              if (tx.getAmount().intValue() < 0) 
              {
                confirmationSvc.createAndSendNegativeAmountNotAllowedMessage(payerPhone, tx)
              } 
              else 
              {

                iou.setExtSystem(ExtSystem.SMS)
                iou.setExtSystemId(sms.getExtSystemId())
                iou.setExtSystemUserName(sms.getSenderPhone())
                iou.setExtSystemDate(sms.getExtSystemDate())
                var payeeHandle: String = "";
                var payerHandle: String = "";
                var payeePhone: JavaLong = null;

                if (tx.getMode().equals("USERNAME")) {
                  payeeHandle = sms.getReceiverUserName()
                } else {
                  payeePhone = new JavaLong(sms.getReceiverPhone())
                }
                //payer
                HttpClient.get[User](userUrl + sms.getSenderPhone()) match {
                  case Some(user) =>
                    System.out.println("payer: " + user.toString())
                    iou.setPayerUserId(user.getId())
                    iou.setPayerFundingStatus(user.getRegStatus())
                    if (user.getRegStatus().equals("NEW_PHONE"))
                      iou.setPayerHandle(payerPhone.toString());
                    else {
                      payerHandle = user.getComPlatform(ExtSystem.FACEBOOK).getUserName();
                      iou.setPayerHandle(payerHandle);
                    }
                  case None =>
                    val resp = userOnboardSvc.createAnonPhone(sms, "payer")
                    iou.setPayerFundingStatus("NEW_PHONE");
                    iou.setPayerHandle(payerPhone.toString());
                }
                //payee phone based
                if (tx.getMode().equals("PHONE")) {
                  HttpClient.get[User](userUrl + sms.getReceiverPhone()) match {
                    case Some(ouser) =>
                      System.out.println("payee: " + ouser.toString())
                      iou.setPayeeUserId(ouser.getId())
                      iou.setPayeeFundingStatus(ouser.getRegStatus());
                      if (ouser.getRegStatus().equals("NEW_PHONE"))
                        iou.setPayeeHandle(payeePhone.toString());
                      else {
                        payeeHandle = ouser.getComPlatform(ExtSystem.FACEBOOK).getUserName();
                        iou.setPayeeHandle(payeeHandle);
                      }
                    case None =>
                      val resp = userOnboardSvc.createAnonPhone(sms, "payee") // <-- Need to grab this mentionee's id ?
                      iou.setPayeeFundingStatus("NEW_PHONE");
                      iou.setPayeeHandle(payeePhone.toString());

                  }
                } //payee userName based
                else {
                  HttpClient.get[User](userNameUrl + sms.getReceiverUserName()) match {
                    case Some(ouser) =>
                      System.out.println("payee: " + ouser.toString())
                      iou.setPayeeUserId(ouser.getId())
                      iou.setPayeeFundingStatus(ouser.getRegStatus());
                      iou.setPayeeHandle(payeeHandle);
                      //assumes payee has phone
                      if (ouser.getPhone() != null) {
                        iou.setMention(ouser.getPhone().toString());
                        payeePhone = ouser.getPhone();
                      } else {
                        //send user onboard message NULL PHONE
                        iou.setPayeeFundingStatus("UNKNOWN_USER");

                      }

                    case None =>
                      //val resp = userOnboardSvc.createAnonFACEBOOK(twitter, "payee") 
                      iou.setPayeeFundingStatus("UNKNOWN_USER");
                      iou.setPayeeHandle(payeeHandle);

                  }
                }
                sms.setStatus("PROCESSED");
                smsSvc.update(sms);
                if (iou.getPayeeFundingStatus().equals("UNKNOWN_USER")) {
                  confirmationSvc.createAndSendUnknownUserMessage(payerPhone, iou, sms.getReceiverUserName());
                  System.out.println("FAILED - UNKNOWN_USER IOU: " + iou.toString());

                } else {
                  iouOrderSvc.createSmsIou(iou);
                  if ((iou.getPayerFundingStatus().equals("DWOLLA_FULL") && iou.getPayeeFundingStatus().equals("DWOLLA_FULL")) ||
                    (iou.getPayerFundingStatus().equals("PAYPAL_FULL") && iou.getPayeeFundingStatus().equals("PAYPAL_FULL"))) {
                    System.out.println("SMS PAY send conf to: " + payerHandle + " and receipt to: " + payeeHandle);
                    val iu = iouOrderSvc.getByExternalSystemIdAndName(iou.getExtSystemId(), ExtSystem.SMS.toString());
                    System.out.println("IOU post-create, pre-confirm: " + iu.toString());
                    iouOrderSvc.updateStatusToConfirm(iu.getId());
                    confirmationSvc.request(payerPhone, iu, payeePhone);

                  } else {
                    if (payeePhone != null) {
                      userOnboardSvc.sendOnboardingMessage(iou, payerPhone, payeePhone);
                      System.out.println("IOU: " + iou.toString());
                    } else {
                      System.out.println("FAILED - null payee phone user IOU: " + iou.toString());

                    }
                  }
                }
              }
            case None => sms.setStatus("PROCESSED"); smsSvc.update(sms); Unit
          }
      }
    }
  }

  def parse(sms: Sms): Option[TransactionIntent] = {
    val p = new TransactionIntent
    p.setDollarlyMention(sms.getDollarlyPhone())
    if (sms.getReceiverUserName() != null) {
      p.setMode("USERNAME")
      p.setMention(sms.getReceiverUserName());
    } else {
      p.setMode("PHONE")
      p.setMention(sms.getReceiverPhone())
    }
    sms.getMessage.split("\\s").foreach(
      { t =>
        if (t.startsWith("$")) {
          PriceParser.parse(t.substring(1)) match {
            case Some(price) => p.setAmount(price)
            case None => Unit
          }
        }
        if (t.startsWith("#")) {
          if (Regex.VALID_HASHTAG.matcher(t).matches())
            p.setHashtag(t.substring(1))
        }
      })
    if (p.matched) Option(p) else None
  }
}
class Smss() extends java.util.ArrayList[Sms] {}
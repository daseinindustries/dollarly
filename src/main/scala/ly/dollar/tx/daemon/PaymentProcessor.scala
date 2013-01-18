package ly.dollar.tx.daemon

import scala.actors.Futures._
import scala.collection.JavaConverters._

import ly.dollar.config._
import ly.dollar.tx._
import ly.dollar.tx.svc._
import ly.dollar.tx.entity._
import ly.dollar.tx.entity.IouOrder.FailReason
import ly.dollar.tx.entity.Payment.Status

import ly.dollar.integrations.dwolla.DwollaIntegrationAPI

// TODO - Rewrite this piece of shit
class PaymentProcessor(iouOrderSvc: IouOrderSvc, paySvc: PaymentSvc,
  msgSvc: ConfirmationSvc, configSvc: ConfigSvc,
  userUrl: String) {

  private var status = AppStatus.RUNNING
  private val throttle = new Throttle(5000L)

  def start() { future { process } }

  def stop() { status = AppStatus.STOPPING; while (status != AppStatus.STOPPED) {} }

  private def process {
    System.out.println("NEW: Polling for unprocessed payments.");
    while (status == AppStatus.RUNNING && throttle.throttle) {
      val payments = paySvc.getAllUnprocessed.asScala.toList
      if (!payments.isEmpty) {
        System.out.println("Found payments to process. " + payments.size)
        payments.sort(
          (p1, p2) => (p1.getCreatedOn compareTo p2.getCreatedOn) > 0).foreach(p => {
            try {
              processOne(p)
            } catch {
              case e: Exception =>
                p.fail(e.getMessage)
                paySvc.update(p)
                e.printStackTrace(System.out)
            }
          })
      }
      if (status == AppStatus.STOPPING) status = AppStatus.STOPPED
    }
  }
  // TODO - make adjustments for twitter vs sms ??
  private def processOne(payment: Payment) {
    System.out.println("PROCESSING PAYMENT: " + payment.getId());
    val po = iouOrderSvc.getById(payment.getPurchaseOrderId)
    val payee = HttpClient.get[User](userUrl + payment.getPayeeUserId).get
    val payer = HttpClient.get[User](userUrl + payment.getPayerUserId).get
    try {
      // This method atomically retrieves the payment and sets its
      // status to 'PROCESSING' if the status is 'UNPROCESSED'
      val lockedPayment = paySvc.lockForProcessing(payment.getId)
      if (lockedPayment != null) {

        paySvc.execute(lockedPayment, payer, payee)
        if (lockedPayment.getStatus == Status.PROCESSED) {
          po.succeed()

          msgSvc.sendSms(payer.getPhone, msgSvc.createReceipt(po, true));
          msgSvc.sendSms(payee.getPhone, msgSvc.createReceipt(po, false));

        } else {
          po.fail(FailReason.FAILED_PAYMENT)
          
          //Dwolla Specific
          if (lockedPayment.getExternalSystem() == ExtSystem.DWOLLA) {
            if (lockedPayment.getExtSystemNotes().contains("Invalid account PIN")) {
              msgSvc.createAndSendInvalidDwollaPin(payer.getPhone(), po)
            } else if (lockedPayment.getExtSystemNotes().contains(
              "Insufficient funds")) {
              msgSvc.createAndSendInsufficientFundsDwolla(payer.getPhone(), po)
            } else {
              msgSvc.createAndSendDwollaOther(payer.getPhone(), po)
            }
          }
          
          //PayPal Specific
            if (lockedPayment.getExternalSystem() == ExtSystem.PAYPAL) {
            if (lockedPayment.getExtSystemNotes().contains("Invalid account PIN")) {
              //msgSvc.createAndSendInvalidDwollaPin(payer.getPhone(), po)
            } else if (lockedPayment.getExtSystemNotes().contains(
              "Insufficient funds")) {
              //msgSvc.createAndSendInsufficientFundsDwolla(payer.getPhone(), po)
            } else {
             // msgSvc.createAndSendDwollaOther(payer.getPhone(), po)
            }
          }
          
        }

        iouOrderSvc.update(po)
        iouOrderSvc.updateOpenSmsTotals(po)
      } else {
        lockedPayment.abort
        paySvc.update(lockedPayment)
        po.fail(FailReason.FAILED_PAYMENT)
      }
      // else, been processed already by another thread or app instance
    } catch {
      case e: Exception =>
        fail(payer, payment, po, FailReason.ERROR)
        e.printStackTrace(System.out)
    }
  }

  private def fail(payer: User, payment: Payment, purchaseOrder: IouOrder, reason: FailReason) {
    //invSvc.add(purchaseOrder.getListingId, purchaseOrder.getNumUnits)
    purchaseOrder.fail(reason)
    iouOrderSvc.update(purchaseOrder)
    msgSvc.sendSms(payer.getPhone,
      "Please go to the Dollar.ly website to correct a " +
        "problem with your payment details.")
  }

}
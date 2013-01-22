package ly.dollar.tx.daemon

import scala.actors.Actor
import scala.actors.Futures._
import scala.collection.JavaConverters._

import java.lang.{ Long => JavaLong }

import org.springframework.social.twitter.api.Tweet
import org.springframework.social.twitter.api.Twitter

import ly.dollar.tx._
import ly.dollar.config._
import ly.dollar.tx.entity._
import ly.dollar.tx.svc._

object TwitterPoller {
  val CountParamValue = 200
  val BaseUrlParams = "?include_rts=false&include_entities=true&count=" + CountParamValue
  val MaxIdParam = "&max_id="
  val SinceIdParam = "&since_id="
}

class TwitterPoller(twitter: Twitter, pollInterval: Long, timelineUrl: String,
  actor: TweetActor, configSvc: ConfigSvc) {

  private var status = AppStatus.RUNNING
  private val throttle = new Throttle(pollInterval)
  private val baseUrl = timelineUrl + TwitterPoller.BaseUrlParams

  def start() { actor.start; future { poll } }

  def stop() { status = AppStatus.STOPPING; while (status != AppStatus.STOPPED) {} }

  def test(t: Tweet) { actor.start; actor ! t }

  private def poll {
    var sinceId = getSinceId();
    while (status == AppStatus.RUNNING && throttle.throttle) {
      val url = if (sinceId == 0) baseUrl
      else baseUrl + TwitterPoller.SinceIdParam + sinceId

      try {
        // Get up to 'count' url param tweets (starts with most recent)
        var tweets = fetchTweets(url)
        if (!tweets.isEmpty) {
          // sinceId indicates the most recent tweet that has been processed,
          // i.e. "respond with tweets newer than since_id" in the next 
          // iteration of the outer loop
          sinceId = setSinceId(tweets.first.getId)

          process(tweets)
          // 'page' back through the timeline and fetch yet unprocessed tweets 
          // by setting the max_id to be the oldest tweet already processed. 
          // max_id can be thought of as like a db cursor
          var maxId = tweets.last.getId
          while (!tweets.isEmpty && status == AppStatus.RUNNING && throttle.throttle) {
            tweets = fetchTweets(url + TwitterPoller.MaxIdParam + maxId)
            if (!tweets.isEmpty) tweets = tweets.tail
            if (!tweets.isEmpty) {
              maxId = tweets.last.getId
              process(tweets)
            }
          }
        }
      } catch {
        case e: Exception => e.printStackTrace(System.out)
      }

      if (status == AppStatus.STOPPING) status = AppStatus.STOPPED
    }
  }

  private def getSinceId() = {
    val sinceId: JavaLong = configSvc.get("twitter.sinceId")
    if (sinceId == null) new JavaLong("0") else sinceId
  }

  private def setSinceId(sinceId: JavaLong) = {
    configSvc.set("twitter.sinceId", sinceId); sinceId
  }

  private def fetchTweets(url: String) = {
    twitter.restOperations()
      .getForObject(url, classOf[Tweets])
      .asScala.toList

  }
  private def process(tweets: List[Tweet]) {
    System.out.println(tweets.size + " tweets fetched.");
    tweets.foreach(t => actor ! t)
  }
}

class TweetActor(iouOrderSvc: IouOrderSvc, userUrl: String, listingUrl: String, userOnboardSvc: UserOnboardSvc, confirmationSvc: ConfirmationSvc) extends Actor {

  def act {
    loop {
      react {
        case tweet: Tweet =>
          parse(tweet) match {
            case Some(tx) =>

              val iou = new IouOrder(tx)
              iou.setExtSystem(ExtSystem.TWITTER)
              iou.setExtSystemId(tweet.getId.toString)
              iou.setExtSystemUserName(tweet.getFromUser.toLowerCase)
              iou.setExtSystemDate(tweet.getCreatedAt)
              //If Payer, exists, set iou.payerUserId
              var phone: JavaLong = new JavaLong("0");
              var payeePhone: JavaLong = new JavaLong("0");
              HttpClient.get[User](userUrl + tweet.getFromUser.toLowerCase) match {
                case Some(user) =>
                  System.out.println("payer: " + user.toString())
                  iou.setPayerUserId(user.getId())
                  iou.setPayerFundingStatus(user.getRegStatus());
                  phone = user.getPhone();

                case None =>
                  val resp = userOnboardSvc.create(tweet.getFromUser.toLowerCase, tweet.getFromUserId())
                  iou.setPayerUserId(resp.getUserId())
                  iou.setPayerFundingStatus("NEW_TWITTER");
              }
              HttpClient.get[User](userUrl + iou.getMention.toLowerCase()) match {
                case Some(ouser) =>
                  System.out.println("payee: " + ouser.toString())
                  iou.setPayeeUserId(ouser.getId())
                  iou.setPayeeFundingStatus(ouser.getRegStatus());
                  payeePhone = ouser.getPhone();
                case None =>
                  val resp = userOnboardSvc.create(iou.getMention(), 0L) // <-- Need to grab this mentionee's id ?
                  iou.setPayeeUserId(resp.getUserId())
                  iou.setPayeeFundingStatus("NEW_TWITTER");
              }
              iouOrderSvc.create(iou);
              if (iou.getPayerFundingStatus().equals("DWOLLA_FULL") && iou.getPayeeFundingStatus().equals("DWOLLA_FULL")) {
                System.out.println("TWEET PAY send conf to: " + phone + " and receipt to: " + payeePhone);
               // var iu = new IouOrder();
                val iu = iouOrderSvc.getByExternalSystemIdAndName(tweet.getId.toString, ExtSystem.TWITTER.toString());
                System.out.println("IOU post-create, pre-confirm: " + iu.toString());
                iouOrderSvc.updateStatusToConfirm(iu.getId());
                confirmationSvc.request(phone, iu, payeePhone);
                
              }
            case None => Unit
          }
        case None => Unit

      }
    }
  }

  def parse(tweet: Tweet): Option[TransactionIntent] = {
    val p = new TransactionIntent
    tweet.getText.split("\\s").foreach(
      { t =>

        if (t.equalsIgnoreCase("@TesTes_1_2_3")) {
          p.setDollarlyMention(t.substring(1).toLowerCase());
        }
        // TODO - Deal with multiple mentions
        if (t.startsWith("@") && !t.equalsIgnoreCase("@TesTes_1_2_3")) {
          p.setMention(t.substring(1).toLowerCase())
        }
        // TODO - Deal with price mismatch
        if (t.startsWith("$")) {
          PriceParser.parse(t.substring(1)) match {
            case Some(price) => p.setAmount(price)
            case None => Unit
          }
        }
        if (t.startsWith("#")) {
          p.setHashtag(t.substring(1))
          val period = t.indexOf(".")
          if (period > -1)
            p.setHashtag(p.getHashtag.substring(0, period))
        }
      })

    if (p.matched) Option(p) else None
  }
}
class Tweets() extends java.util.ArrayList[Tweet] {}
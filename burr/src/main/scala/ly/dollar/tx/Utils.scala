package ly.dollar.tx

import java.net.URI
import java.math.BigDecimal
import java.lang.{ Long => JavaLong }

import ly.dollar.tx.entity.DollarlyUserResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

class Throttle(intervalInMillis: Long) {

  var lastCallTime = System.currentTimeMillis

  def throttle = {
    while (System.currentTimeMillis - lastCallTime < intervalInMillis) {}
    lastCallTime = System.currentTimeMillis
    true
  }

}

object AppStatus extends Enumeration {

  type Status = Value

  val RUNNING, STOPPING, STOPPED = Value

}

object PriceParser {
  def parse(s: String) = {
    try {
      var t = s.replaceAll(",", "")
      Some(new BigDecimal(t).setScale(2, BigDecimal.ROUND_HALF_UP))
    } catch { case _ => None }
  }
}

object NumberParser {

  def parseInt(s: String) = try { Some(s.toInt) } catch { case _ => None }

  def parseDouble(s: String) = try { Some(s.toDouble) } catch { case _ => None }

  def parseBigDecimal(s: String) = try { Some(new BigDecimal(s)) } catch { case _ => None }

}

object HttpClient {

  def get[T: Manifest](url: String): Option[T] = {
    get[T](url, "")
  }

  def get[T: Manifest](url: String, param: String): Option[T] = {
    System.out.println("Fetching " + url + param)
    try {
      val httpClient = new DefaultHttpClient()
	  val credentials = new UsernamePasswordCredentials("hamilton", "Wi11u|$hit53$Bs4m3");
	  httpClient.getCredentialsProvider().setCredentials(org.apache.http.auth.AuthScope.ANY, credentials)
	  val clientExecutor = new ApacheHttpClient4Executor(httpClient)
      val factory = new ClientRequestFactory(clientExecutor, new URI(url)) 
	  val req = factory.createRequest(url + param)
	  //val req = new ClientRequest(url + param)
      req.accept("application/json")
      Option(req.getTarget(manifest[T].erasure).asInstanceOf[T])
    } catch {
      case e: Exception =>
        //e.printStackTrace(System.out)
        None
    }
  }
   def put[T: Manifest](url: String, qparam: String): Option[T] = {
    System.out.println("Putting " + url + qparam)
    try {
      val httpClient = new DefaultHttpClient()
	  val credentials = new UsernamePasswordCredentials("hamilton", "Wi11u|$hit53$Bs4m3");
	  httpClient.getCredentialsProvider().setCredentials(org.apache.http.auth.AuthScope.ANY, credentials)
	  val clientExecutor = new ApacheHttpClient4Executor(httpClient)
      val factory = new ClientRequestFactory(clientExecutor, new URI(url)) 
	  val req = factory.createRequest(url + qparam)
      req.accept("application/json")
      Option(req.put(manifest[T].erasure).asInstanceOf[T])
    } catch {
      case e: Exception =>
        e.printStackTrace(System.out)
        None
    }
  }

 
}
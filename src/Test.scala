import java.io.FileInputStream
import java.security.{KeyStore, SecureRandom}

import akka.actor.ActorSystem
import akka.http.scaladsl.{ConnectionContext, Http, Http2}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Test extends App {
  implicit val actorSystem: ActorSystem = ActorSystem("test")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  try {
    val keystore = KeyStore.getInstance("PKCS12")
    val stream = new FileInputStream("server.p12")
    try keystore.load(stream, "12345678".toCharArray)
    finally stream.close()

    val keyManager = KeyManagerFactory.getInstance("SunX509")
    keyManager.init(keystore, "12345678".toCharArray)

    val trustManager = TrustManagerFactory.getInstance("SunX509")
    trustManager.init(keystore)

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManager.getKeyManagers, trustManager.getTrustManagers, SecureRandom.getInstanceStrong)

    val connectionContext = ConnectionContext.https(sslContext)

    val routes: Route = complete(s"I'm alive! Timestamp: ${System.currentTimeMillis()}\n")
    val asyncHandler: HttpRequest => Future[HttpResponse] = Route.asyncHandler(routes)

    val httpFuture = Http().bindAndHandleAsync(asyncHandler, "0.0.0.0", 8080, connectionContext)
    val http2Future = Http2().bindAndHandleAsync(asyncHandler, "0.0.0.0", 8081, connectionContext)

    println("Waiting for TLS servers to start...")
    val http = Await.result(httpFuture, Duration.Inf)
    val http2 = Await.result(http2Future, Duration.Inf)

    println("HTTP/1.1 server is listening on http://localhost:8080 or https://localhost:8080")
    println("HTTP/2 server is listening on http://localhost:8081 or https://localhost:8081")
    println("TLS version of both servers works normally, but unencrypted version hangs indefinitely until connection timeouts.")
    println("Press <Enter> to stop everything.")

    Console.in.readLine()

    println("Terminating servers...")
    Await.result(http.terminate(5 second span), 10 second span)
    Await.result(http2.terminate(5 second span), 10 second span)
    Await.result(actorSystem.terminate(), 10 second span)
  } catch {
    case e: Exception =>
      e.printStackTrace()
      sys.exit(1)
  }
}

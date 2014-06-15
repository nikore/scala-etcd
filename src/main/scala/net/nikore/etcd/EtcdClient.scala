package net.nikore.etcd

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.io.IO
import spray.can.Http
import spray.client.pipelining._
import spray.util._
import scala.concurrent.Future
import EtcdJsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.http.{Uri, HttpRequest, HttpResponse}
import EtcdExceptions._
import java.net.URLEncoder
import net.nikore.etcd.EtcdJsonProtocol.EtcdResponse
import net.nikore.etcd.EtcdJsonProtocol.EtcdListResponse

class EtcdClient(conn: String) {

  private val baseUrl = conn + "/v2/keys/"
  implicit val system = ActorSystem("etcd-client")
  import system.dispatcher

  def getKey(key: String): Future[EtcdResponse] = {
    getKeyAndWait(key, wait = false)
  }

  def getKeyAndWait(key: String, wait: Boolean = true): Future[EtcdResponse] = {
    defaultPipeline(Get(baseUrl + key + "?wait=" + wait))
  }

  def setKey(key: String, value: String): Future[EtcdResponse] = {
    val encodedString = URLEncoder.encode(value, "UTF-8")
    defaultPipeline(Put(Uri(baseUrl + key + "?value=" + encodedString, Uri.ParsingMode.RelaxedWithRawQuery)))
  }

  def deleteKey(key: String): Future[EtcdResponse] = {
    defaultPipeline(Delete(baseUrl + key))
  }

  def createDir(dir: String): Future[EtcdResponse] = {
    defaultPipeline(Put(baseUrl + dir + "?dir=true"))
  }

  def listDir(dir: String, recursive: Boolean = false): Future[EtcdListResponse] = {
    val pipline: HttpRequest => Future[EtcdListResponse] = (
      sendReceive
        ~> mapErrors
        ~> unmarshal[EtcdListResponse]
      )

    pipline(Get(baseUrl + dir + "/?recursive=" + recursive))
  }

  def deleteDir(dir: String, recursive: Boolean = false): Future[EtcdResponse] = {
    defaultPipeline(Delete(baseUrl + dir + "?recursive=" + recursive))
  }

  private val mapErrors = (response: HttpResponse) => {
    if (response.status.isSuccess) response
    else {
      response.entity.asString.parseJson.convertTo[Error] match {
        case e if e.errorCode == 100 => throw KeyNotFoundException(e.message, "not found", e.index)
        case e => throw new RuntimeException("General error: " + e.toString)
      }
    }
  }

  private val defaultPipeline: HttpRequest => Future[EtcdResponse] = (
    sendReceive
      ~> mapErrors
      ~> unmarshal[EtcdResponse]
    )

  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(1.second).await
    system.shutdown()
  }
}

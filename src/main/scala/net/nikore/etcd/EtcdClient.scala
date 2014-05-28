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
import spray.http.HttpRequest
import spray.http.HttpResponse
import EtcdExceptions._
import java.net.URLEncoder
import net.nikore.etcd.EtcdJsonProtocol.EtcdResponse
import net.nikore.etcd.EtcdJsonProtocol.EtcdListResponse

class EtcdClient(conn: String) {

  implicit val system = ActorSystem("etcd-client")
  import system.dispatcher

  def getKey(key: String): Future[EtcdResponse] = {
    getKeyWait(key, wait = false)
  }

  def getKeyWait(key: String, wait: Boolean): Future[EtcdResponse] = {
    defaultPipeline(Get(conn + "/v2/keys/" + key + "?wait=" + wait))
  }

  def setKey(key: String, value: String): Future[EtcdResponse] = {
    val encodedString = URLEncoder.encode(value, "UTF-8")
    defaultPipeline(Put(conn + "/v2/keys/" + cleanString(key) + "?value=" + encodedString))
  }

  def deleteKey(key: String): Future[EtcdResponse] = {
    defaultPipeline(Delete(conn + "/v2/keys/" + key))
  }

  def createDir(dir: String): Future[EtcdResponse] = {
    defaultPipeline(Put(conn + "/v2/keys/" + dir + "?dir=true"))
  }

  def listDir(dir: String): Future[EtcdListResponse] = {
    val pipline: HttpRequest => Future[EtcdListResponse] = (
      sendReceive
        ~> mapErrors
        ~> unmarshal[EtcdListResponse]
      )

    pipline(Get(conn + "/v2/keys/" + cleanString(dir) + "/"))
  }

  def deleteDir(dir: String): Future[EtcdResponse] = {
    defaultPipeline(Delete(conn + "/v2/keys/" + dir + "?recursive=true"))
  }

  private def cleanString(str: String): String = {
    str match {
      case s if s.startsWith("/") => s.stripPrefix("/")
      case _ => str
    }
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

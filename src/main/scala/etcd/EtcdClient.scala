package etcd

import scala.util.{Success, Failure}
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.event.Logging
import akka.io.IO
import spray.json.{JsonFormat, DefaultJsonProtocol}
import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import spray.util._
import spray.http.{HttpResponse, HttpRequest}

case class NodeResponse(key: String, value: String, modifiedIndex: Int, createdIndex: Int)
case class EtcdResponse(action: String, node: NodeResponse)

object EtcdJsonProtocol extends DefaultJsonProtocol {
  implicit val nodeResponseFormat = jsonFormat4(NodeResponse)
  implicit val etcdResponseFormat = jsonFormat2(EtcdResponse)
}

class EtcdClient(conn: String) {

  implicit val system = ActorSystem("etcd-client")
  import system.dispatcher
  import EtcdJsonProtocol._
  import SprayJsonSupport._
  val pipeline = sendReceive ~> unmarshal[EtcdResponse]

  def get(key: String) = request(Get(conn + "/v2/keys/" + key))

  def set(key: String, value: String) = request(Put(conn + "/v2/keys/" + key +"?value=" + value))

  def request(request: HttpRequest) = {
    val response = pipeline{request}
    response onComplete {
      case Success(stuff: EtcdResponse) =>
        println(stuff.node.value)

      case Success(somethingUnexpected) =>
        print("something unexpected")

      case Failure(error) =>
        println(error.getMessage)
    }
  }

  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(1.second).await
    system.shutdown()
  }
}

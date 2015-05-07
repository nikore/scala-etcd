package net.nikore.etcd

import akka.actor.ActorSystem

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import net.nikore.etcd.EtcdJsonProtocol.EtcdResponse
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object EtcdClientSmokeTest extends App {
  val system = ActorSystem("test-etcd")
  val client = EtcdClient("http://localhost:4001")(system)

  client.getKey("test4").map(println) recover {
    case ex: Exception => println(ex)
  }

  val response: Future[EtcdResponse] = client.setKey("test4","com.java.property1=value2\n" +
    "com.java.property3=vale4\n" +
    "com.java.property4=value6\n" +
    "com.java.property5=value10\n" +
    "com.java.property6=value11;value12;value13\n" +
    "com.java.property7=value14&value16&value17\n", Some(5 seconds)
  )

  //  val response: Future[EtcdResponse] = client.setKey("test3","yep")

  response map {response: EtcdResponse =>
    println(response)
    println()
  }  recover {
    case ex: Exception =>
      System.out.println(ex)
  } map (_ =>
      client.getKey("test4") map(println) andThen {
        case _ =>
          println("shutting down...")
          system.shutdown()
      }
  )
}

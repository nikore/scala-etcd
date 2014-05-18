package net.nikore.etcd

import scala.concurrent.Future
import net.nikore.etcd.EtcdJsonProtocol.EtcdResponse
import scala.util.{Failure, Success}

object EtcdClientSmokeTest {
  val client = new EtcdClient("http://localhost:4001")

  val response: Future[EtcdResponse] = client.setKey("test4","com.java.property1=value2\n" +
    "com.java.property3=vale4\n" +
    "com.java.property4=value6\n" +
    "com.java.property5=value10\n"
  )

  //  val response: Future[EtcdResponse] = client.setKey("test3","yep")

  response onComplete {
    case Success(response: EtcdResponse) =>
      System.out.println(response)
      client.shutdown()
    case Failure(error) =>
      System.out.println(error)
      client.shutdown()
  }
}
